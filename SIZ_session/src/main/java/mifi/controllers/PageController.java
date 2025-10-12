package mifi.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping(
            path = {
                    "",
                    "/",
                    "/home"
            }
    )
    public String home()  {
        return "index.html";
    }

    @GetMapping("/public")
    public String publicPage()  {
        return "public.html";
    }

    @GetMapping("/protected")
    public String protectedPage(@AuthenticationPrincipal DefaultOAuth2User principal, Model model)  {
        if (principal != null) {
            model.addAttribute("name", principal.getAttributes().getOrDefault("name", "Unknown"));
            model.addAttribute("bio", principal.getAttributes().getOrDefault("bio", "Unknown"));
        }
        return "protected";
    }

}
