package xyz.parti.catan.models;

/**
 * Created by dalikim on 2017. 3. 27..
 */

public class PartiAccessToken {
    public String access_token;
    public String token_type;
    public Long expires_in;
    public String refresh_token;

    public String getValidTokenType() {
        // OAuth requires uppercase Authorization HTTP header value for token type
        if(!Character.isUpperCase(token_type.charAt(0))) {
            token_type = Character.toString(token_type.charAt(0)).toUpperCase() + token_type.substring(1);
        }

        return token_type;
    }
}
