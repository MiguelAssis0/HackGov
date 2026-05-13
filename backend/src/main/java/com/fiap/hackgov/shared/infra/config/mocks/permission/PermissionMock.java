package com.fiap.hackgov.shared.infra.config.mocks.permission;

import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.Permissions;
import com.fiap.hackgov.cityhall_management.internal.entities.PermissionsOccupation;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.Actions;
import com.fiap.hackgov.cityhall_management.internal.repositories.PermissionsOccupationRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.PermissionsRepository;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionMock {

    private final PermissionsRepository permissionsRepository;
    private final PermissionsOccupationRepository permissionsOccupationRepository;

    public void load(MockContext ctx) {

        // -------------------------
        // Permissions
        // -------------------------

        Permissions permRead = createPermission("SECTOR", Actions.READ);

        Permissions permCreate = createPermission("SECTOR", Actions.CREATE);

        Permissions permDelete = createPermission("SECTOR", Actions.DELETE);

        Permissions permUpdate = createPermission("SECTOR", Actions.UPDATE);

        permissionsRepository.saveAll(List.of(permRead, permCreate, permDelete, permUpdate));

        // -------------------------
        // Occupation Permissions
        // -------------------------

        List<PermissionsOccupation> relations = List.of(

                // Analista
                createRelation(permRead, ctx.analista),

                createRelation(permCreate, ctx.analista),

                // Gerente
                createRelation(permRead, ctx.gerente),

                createRelation(permCreate, ctx.gerente),

                createRelation(permDelete, ctx.gerente),

                createRelation(permUpdate, ctx.gerente),

                // Assistente
                createRelation(permRead, ctx.assistente));

        permissionsOccupationRepository.saveAll(relations);
    }

    private Permissions createPermission(String resource, Actions action) {

        Permissions permission = new Permissions();

        permission.setResource(resource);

        permission.getAction().add(action);

        return permission;
    }

    private PermissionsOccupation createRelation(Permissions permission, Occupation occupation) {

        return new PermissionsOccupation(permission, occupation);
    }
}