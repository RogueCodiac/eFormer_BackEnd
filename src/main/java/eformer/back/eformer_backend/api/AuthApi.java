package eformer.back.eformer_backend.api;

import eformer.back.eformer_backend.api.request.AuthenticationRequest;
import eformer.back.eformer_backend.api.request.RegisterRequest;
import eformer.back.eformer_backend.api.response.AuthenticationResponse;
import eformer.back.eformer_backend.utility.auth.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth/")
public class AuthApi {
    private final AuthenticationService authService;

    public AuthApi(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("register")
    @ResponseBody
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        try {
            /* 200 */
            return new ResponseEntity<>(authService.register(request), HttpStatus.OK);
        } catch (Exception ignored) {
            /* 400 */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("authenticate")
    @ResponseBody
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        try {
            /* 200 */
            return new ResponseEntity<>(authService.authenticate(request), HttpStatus.OK);
        } catch (Exception ignored) {
            /* 400 */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}