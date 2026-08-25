@FilterDefs({
        @FilterDef(
                name = "cityHallFilter",
                parameters = @ParamDef(name = "cityHallId", type = UUID.class),
                applyToLoadByKey = true
        ),
        @FilterDef(
                name = "sectorFilter",
                parameters = @ParamDef(name = "sectorId", type = UUID.class),
                applyToLoadByKey = true
        )
})
package com.fiap.hackgov; // coloque no package raiz do projeto

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;
