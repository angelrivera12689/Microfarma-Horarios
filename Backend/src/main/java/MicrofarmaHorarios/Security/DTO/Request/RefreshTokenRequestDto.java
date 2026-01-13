package MicrofarmaHorarios.Security.DTO.Request;

public class RefreshTokenRequestDto {

    private String refreshToken;

    public RefreshTokenRequestDto() {}

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}