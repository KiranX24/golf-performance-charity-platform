package com.digitalheroes.config;

import com.digitalheroes.entity.*;
import com.digitalheroes.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;

@Configuration
public class DataInitializer {
    private final SubscriptionPlanRepository plans;
    private final CharityRepository charities;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.demo.enabled:true}") boolean enabled;
    @Value("${app.demo.user-email:demo@digitalheroes.local}") String userEmail;
    @Value("${app.demo.user-password:Demo@12345}") String userPassword;
    @Value("${app.demo.admin-email:admin@digitalheroes.local}") String adminEmail;
    @Value("${app.demo.admin-password:Admin@12345}") String adminPassword;

    public DataInitializer(SubscriptionPlanRepository plans, CharityRepository charities, UserRepository users, PasswordEncoder encoder) {
        this.plans = plans;
        this.charities = charities;
        this.users = users;
        this.encoder = encoder;
    }

    @Bean
    CommandLineRunner seed(){
        return args->{
            if(!enabled)return;
            if(plans.count()==0){
                SubscriptionPlan m=new SubscriptionPlan(); m.setName("Monthly Hero"); m.setPlanInterval(PlanInterval.MONTHLY); m.setPrice(new BigDecimal("499")); plans.save(m);
                SubscriptionPlan y=new SubscriptionPlan(); y.setName("Yearly Hero"); y.setPlanInterval(PlanInterval.YEARLY); y.setPrice(new BigDecimal("4999")); plans.save(y);
            }
            if(charities.count()==0){
                Charity a=new Charity(); a.setName("Green Earth Foundation"); a.setSlug("green-earth-foundation"); a.setDescription("Community-led environmental restoration and education."); a.setFeatured(true); charities.save(a);
                Charity b=new Charity(); b.setName("Hope for Children"); b.setSlug("hope-for-children"); b.setDescription("Education and nutrition support for children."); charities.save(b);
            }
            create(userEmail,userPassword,"Demo User",Role.ROLE_USER);
            create(adminEmail,adminPassword,"Digital Heroes Admin",Role.ROLE_ADMIN);
        };
    }

    private void create(String e,String p,String n,Role r){
        if(users.findByEmailIgnoreCase(e).isEmpty()){
            User u=new User(); u.setEmail(e); u.setPasswordHash(encoder.encode(p)); u.setFullName(n); u.setRole(r); users.save(u);
        }
    }
}
