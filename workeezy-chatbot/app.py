import os
import re
import requests
from flask import Flask, request, jsonify

app = Flask(__name__)

# ✅ 너 로컬 스프링 서버 주소(필요하면 바꿔)
BACKEND_BASE = os.getenv("BACKEND_BASE", "http://127.0.0.1:8080")
# ✅ 상세 링크를 걸고 싶으면 프론트 주소(선택)
FRONT_BASE = os.getenv("FRONT_BASE", "http://127.0.0.1:5173")

# 지역 목록 (프로젝트에서 쓰는 지역과 맞추면 됨)
REGIONS = [
    "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
    "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
]

# 사용자가 같이 붙여 말하는 단어들 제거용
STOPWORDS = [
    "숙소", "오피스", "워크", "워케이션", "여행", "근처", "추천",
    "찾아줘", "알려줘", "있어", "뭐야", "보여줘", "검색", "찾기",
    "프로그램", "상품"
]

def kakao_simple(text: str):
    return {
        "version": "2.0",
        "template": {"outputs": [{"simpleText": {"text": text}}]},
    }

def kakao_list_card(title: str, items: list):
    return {
        "version": "2.0",
        "template": {
            "outputs": [{
                "listCard": {
                    "header": {"title": title},
                    "items": items
                }
            }]
        }
    }

def normalize_keyword(text: str) -> str:
    """
    예) "제주 숙소" -> "제주"
        "강릉 오피스 추천" -> "강릉"
        "워케이션 알려줘" -> "워케이션"(지역 없으면 남은 키워드)
    """
    t = (text or "").strip()
    if not t:
        return ""

    # 1) 지역이 문장에 들어있으면 지역을 최우선 키워드로
    region = next((r for r in REGIONS if r in t), None)
    if region:
        return region

    # 2) 지역이 없으면 불용어 제거 후 남은 값
    for w in STOPWORDS:
        t = t.replace(w, " ")
    t = re.sub(r"\s+", " ", t).strip()

    return t

def pick_field(d: dict, *keys, default=None):
    for k in keys:
        v = d.get(k)
        if v not in (None, ""):
            return v
    return default

@app.get("/")
def health():
    return "OK"

@app.route("/skill/search_program", methods=["POST"])
def search_program():
    req = request.get_json(silent=True) or {}

    # ✅ 1) keyword 추출 (detailParams 우선 / 없으면 utterance)
    params = (req.get("action") or {}).get("detailParams") or {}
    keyword_raw = (params.get("keyword") or {}).get("value") \
        or (req.get("userRequest", {}) or {}).get("utterance") \
        or ""

    keyword_raw = (keyword_raw or "").strip()
    keyword = normalize_keyword(keyword_raw)

    # 디버그 로그
    print("=== KAKAO REQ ===")
    print("utterance/raw:", keyword_raw)
    print("normalized:", keyword)

    if not keyword:
        return jsonify(kakao_simple("검색어를 입력해주세요. 예) 제주, 강릉, 부산"))

    # ✅ 2) Spring 챗봇 전용 검색 API 호출
    try:
        r = requests.get(
            f"{BACKEND_BASE}/api/chat/search",
            params={"keyword": keyword},
            timeout=6
        )

        if r.status_code in (401, 403):
            # permitAll인데도 401이면 security/필터 문제이므로 명확히 표기
            return jsonify(kakao_simple("검색 API가 인증에 막혔어요. /api/chat/** permitAll + JWT 필터 스킵을 확인해주세요."))

        r.raise_for_status()
        data = r.json()

    except requests.exceptions.RequestException as e:
        return jsonify(kakao_simple(f"검색 서버 연결 실패: {e}"))

    # ✅ 3) 결과 파싱
    cards = data.get("cards") or data.get("results") or data.get("items") or []
    if not cards:
        return jsonify(kakao_simple(f"'{keyword_raw}' 검색 결과가 없어요 😥"))

    # ✅ 4) 카카오 listCard 변환 (최대 5개)
    items = []
    for c in cards[:5]:
        program_id = pick_field(c, "programId", "id", "program_id")
        title = pick_field(c, "programTitle", "title", "name", default="프로그램")
        region = pick_field(c, "region", "area", "location", default="")
        price = pick_field(c, "price", "programPrice", "minPrice", default=None)

        desc_parts = []
        if region:
            desc_parts.append(region)
        if isinstance(price, (int, float)):
            desc_parts.append(f"{int(price):,}원")
        desc = " · ".join(desc_parts) if desc_parts else " "

        item = {"title": title, "description": desc}

        # 상세 링크 (프론트 라우팅이 /program/{id} 라면)
        if program_id is not None:
            item["link"] = {"web": f"{FRONT_BASE}/program/{program_id}"}

        items.append(item)

    header = f"'{keyword_raw}' 검색 결과"
    return jsonify(kakao_list_card(header, items))


if __name__ == "__main__":
    # 로컬에서는 이대로 OK
    app.run(host="127.0.0.1", port=8000, debug=True)
