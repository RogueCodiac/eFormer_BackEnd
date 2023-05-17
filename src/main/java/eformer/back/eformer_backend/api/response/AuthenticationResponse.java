package eformer.back.eformer_backend.api.response;


public class AuthenticationResponse {
    private String token;

    public AuthenticationResponse(String token) {
        setToken(token);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
