package yuhan.pro.mainserver.domain.auth.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class OAuth2Constants {

    // GitHub API URLs
    public static final String GITHUB_ORGS_URL = "https://api.github.com/user/memberships/orgs";
    public static final String GITHUB_EMAILS_URL = "https://api.github.com/user/emails";

    // GitHub OAuth Attributes
    public static final String ATTR_ID = "id";
    public static final String ATTR_LOGIN = "login";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_AVATAR_URL = "avatar_url";

    // GitHub Email Fields
    public static final String FIELD_PRIMARY = "primary";
    public static final String FIELD_VERIFIED = "verified";
    public static final String FIELD_EMAIL = "email";
}

