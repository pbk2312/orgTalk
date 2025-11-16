package yuhan.pro.mainserver.sharedkernel.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CookieUtils {

    public static void addCookie(HttpServletResponse response, String name, String value,
            int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        // 도메인 설정 제거 (서브도메인 간 쿠키 공유 문제 방지)
        // cookie.setDomain()을 설정하지 않으면 현재 도메인에만 쿠키가 설정됨
        response.addCookie(cookie);
    }
    
    public static void removeCookie(HttpServletResponse response, String name) {
        addCookie(response, name, null, 0);
    }
}
