package yuhan.pro.mainserver.sharedkernel.common.constants;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class JwtConstants {

    // JWT Claims
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_MEMBER_ID = "memberId";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_ORG_IDS = "orgIds";
    
    // Role Prefixes
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ORG_ROLE_PREFIX = "ROLE_ORG_";
    public static final String DEFAULT_PASSWORD = "PASSWORD";

    // HTTP Headers
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // Redis Keys
    public static final String REFRESH_TOKEN_PREFIX = "refreshToken:";
    
    // Cookie Names
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    
    public static Set<Long> convertOrgIdsToSet(List<Integer> orgList) {
        if (orgList == null) {
            return Set.of();
        }
        return orgList.stream()
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }
}

