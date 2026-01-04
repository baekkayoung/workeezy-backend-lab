package com.together.workeezy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {

    // 로그인 세션용
    @Bean(name = "loginRedisTemplate")
    public RedisTemplate<String, String> loginRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 안전하게 하려면 직렬화 설정 추가
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    // 임시 저장용
    @Bean(name = "draftRedisTemplate")
    public RedisTemplate<String, Object> draftRedisTemplate(RedisConnectionFactory connectionFactory) {

        // 키:문자열, 값:객체인 레디스 템플릿 생성
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 해당 템플릿에 커넥션 공장을 설정하는데, 위에서 만든 Lettuce 커낵션 팩토리로 설정
        template.setConnectionFactory(connectionFactory);

        // Json 직렬화 설정
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();

        // 기계가 데이터를 JSON으로 바꿀 때 객체의 타입 정보도 같이 붙게 만드는 설정

//        objectMapper.activateDefaultTyping(
//                LaissezFaireSubTypeValidator.instance, // 웬만한 타입 다 허용
//                ObjectMapper.DefaultTyping.NON_FINAL //final이 아닌 클래스(예: DTO, List 등)에 타입정보를 붙여라.
//        );
        //이 objectMapper를 JSON 변환용으로 씀
        serializer.setObjectMapper(objectMapper);

        // Redis에 저장할 키/벨류 방식을 지정
        template.setKeySerializer(new StringRedisSerializer()); // 문자열 그대로 저장
        template.setValueSerializer(serializer); // 내가 만든 Json 직렬기로 처리
        template.afterPropertiesSet(); // 이제 설정 끝났으니까 초기화 후 준비!
        return template;
    }
}
