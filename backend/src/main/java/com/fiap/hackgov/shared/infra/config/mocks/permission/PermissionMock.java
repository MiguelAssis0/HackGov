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

        Permissions approveSecretary = createPermission("approval.secretary", Actions.UPDATE);

        Permissions approveProcurement = createPermission("approval.procurement", Actions.UPDATE);

        Permissions approvePayment = createPermission("approval.payment", Actions.UPDATE);

        Permissions approveAccountability = createPermission("approval.accountability", Actions.UPDATE);

        permissionsRepository.saveAll(List.of(
                permRead,
                permCreate,
                permDelete,
                permUpdate,
                approveSecretary,
                approveProcurement,
                approvePayment,
                approveAccountability
        ));

        // -------------------------
        // Occupation Permissions
        // -------------------------

        List<PermissionsOccupation> relations = List.of(

                // Administrador municipal
                createRelation(permRead, ctx.administradorMunicipal),
                createRelation(permCreate, ctx.administradorMunicipal),
                createRelation(permDelete, ctx.administradorMunicipal),
                createRelation(permUpdate, ctx.administradorMunicipal),
                createRelation(approveSecretary, ctx.administradorMunicipal),
                createRelation(approveProcurement, ctx.administradorMunicipal),
                createRelation(approvePayment, ctx.administradorMunicipal),
                createRelation(approveAccountability, ctx.administradorMunicipal),

                // Analista
                createRelation(permRead, ctx.analista),

                createRelation(permCreate, ctx.analista),

                // Gerente
                createRelation(permRead, ctx.gerente),

                createRelation(permCreate, ctx.gerente),

                createRelation(permDelete, ctx.gerente),

                createRelation(permUpdate, ctx.gerente),

                // Agente de compras
                createRelation(permRead, ctx.agenteCompras),
                createRelation(permCreate, ctx.agenteCompras),
                createRelation(permUpdate, ctx.agenteCompras),

                // Pregoeiro
                createRelation(permRead, ctx.pregoeiro),
                createRelation(permCreate, ctx.pregoeiro),
                createRelation(permUpdate, ctx.pregoeiro),
                createRelation(approveProcurement, ctx.pregoeiro),

                // Analista financeiro
                createRelation(permRead, ctx.analistaFinanceiro),
                createRelation(permUpdate, ctx.analistaFinanceiro),
                createRelation(approvePayment, ctx.analistaFinanceiro),
                createRelation(approveAccountability, ctx.analistaFinanceiro),

                // Gestor de contratos
                createRelation(permRead, ctx.gestorContratos),
                createRelation(permCreate, ctx.gestorContratos),
                createRelation(permUpdate, ctx.gestorContratos),

                // Assessor jurídico
                createRelation(permRead, ctx.assessorJuridico),
                createRelation(permUpdate, ctx.assessorJuridico),

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
