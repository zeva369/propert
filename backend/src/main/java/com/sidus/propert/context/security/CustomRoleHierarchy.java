package com.sidus.propert.context.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class CustomRoleHierarchy implements RoleHierarchy {

    private final Map<String, List<String>> roleHierarchyMap = new HashMap<>();

    public CustomRoleHierarchy() {
        roleHierarchyMap.put("ROLE_ADMIN", Arrays.asList("ROLE_USER", "ROLE_GUEST"));
        roleHierarchyMap.put("ROLE_USER", Collections.singletonList("ROLE_GUEST"));
        roleHierarchyMap.put("ROLE_GUEST", Collections.emptyList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> reachableAuthorities = new HashSet<>(authorities);

        for (GrantedAuthority authority : authorities) {
            String roleName = authority.getAuthority();
            List<String> childRoles = roleHierarchyMap.getOrDefault(roleName, Collections.emptyList());
            for (String childRole : childRoles) {
                reachableAuthorities.add(new SimpleGrantedAuthority(childRole));
            }
        }
        return reachableAuthorities;
    }
}