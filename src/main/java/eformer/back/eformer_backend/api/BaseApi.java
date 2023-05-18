package eformer.back.eformer_backend.api;

import eformer.back.eformer_backend.model.User;
import eformer.back.eformer_backend.repository.UserRepository;
import eformer.back.eformer_backend.utility.auth.JwtService;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class BaseApi {
    final JwtService jService;

    final UserRepository userRepo;

    public BaseApi(JwtService jService, UserRepository userRepo) {
        this.jService = jService;
        this.userRepo = userRepo;
    }

    public User extractUser(HashMap<String, String> header) {
        return userRepo
                .findByUsername(jService.extractUsername(header.get("authorization").split(" ")[1]))
                .orElseThrow();
    }

    public boolean canUserChange(HashMap<String, String> header) {
        var user = extractUser(header);

        return user.isEmployee() || user.isManager();
    }
}
