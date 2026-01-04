import os
import requests
from flask import Flask, request, jsonify
print("### LOADED app.py (build=CHATBOT-20260102-1) ###", flush=True)

app = Flask(__name__)

SPRING_BASE = os.getenv("SPRING_BASE_URL", "http://workeezy-backend:8080")
BUILD_TAG = os.getenv("BUILD_TAG", "chatbot-v3")  # ✅ 응답에 찍어서 코드 반영 확인용

def kakao_text(text: str):
    return {
        "version": "2.0",
        "template": {"outputs": [{"simpleText": {"text": text}}]},
    }

def kakao_list(title: str, items: list[dict], buttons: list[dict] | None = None):
    card = {
        "header": {"title": title},
        "items": items[:5],
    }
    if buttons:
        card["buttons"] = buttons

    return {
        "version": "2.0",
        "template": {"outputs": [{"listCard": card}]},
    }


def extract_keyword(req: dict) -> str:
    # ✅ 카카오가 보내는 위치가 케이스마다 달라서 전부 커버
    action = req.get("action") or {}
    params = action.get("params") or {}
    detail = action.get("detailParams") or {}

    # 1) action.params.keyword
    kw = params.get("keyword")
    if isinstance(kw, str) and kw.strip():
        return kw.strip()

    # 2) action.detailParams.keyword.value
    kw_obj = detail.get("keyword") or {}
    if isinstance(kw_obj, dict):
        v = kw_obj.get("value")
        if isinstance(v, str) and v.strip():
            return v.strip()

    # 3) userRequest.utterance fallback
    utter = ((req.get("userRequest") or {}).get("utterance") or "")
    if isinstance(utter, str) and utter.strip():
        return utter.strip()

    return ""

@app.get("/")
def health():
    return "OK"

@app.post("/skill/search_program")
def search_program():
    req = request.get_json(silent=True) or {}
    keyword = extract_keyword(req)

    if not keyword:
        return jsonify(kakao_text("검색어를 입력해주세요. 예: 제주, 부산, 오피스"))

    try:
        r = requests.get(
            f"{SPRING_BASE}/api/chat/search",
            params={"keyword": keyword},
            timeout=5,
        )
        r.raise_for_status()
        data = r.json()
    except Exception as e:
        return jsonify(kakao_text(f"검색 중 오류가 발생했어요 😢\n{e}"))

    cards = data.get("cards") or []
    if not cards:
        return jsonify(kakao_text(f"'{keyword}' 검색 결과가 없어요 😢"))

    items = []
    for c in cards[:5]:
        pid = c.get("id")
        title = c.get("title", "")
        region = c.get("region", "")
        price = c.get("price", 0)
        photo = c.get("photo")

        desc = f"{region} · {price:,}원" if price else region

        item = {
            "title": title,
            "description": desc,
        }

        # 이미지 (절대 URL이면 그대로 사용)
        if photo:
            item["imageUrl"] = photo



    buttons = [
        {
            "label": "전체 결과 보기",
            "action": "webLink",
            "webLinkUrl": f"https://workeezy.cloud/search?keyword={keyword}",
        }
    ]

    return jsonify(
        kakao_list(f"'{keyword}' 검색 결과", items, buttons)
    )


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000, debug=True)
