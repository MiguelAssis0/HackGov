package com.fiap.hackgov.shared.infra.config.mocks.task;

import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import com.fiap.hackgov.tasks.internal.entities.Board;
import com.fiap.hackgov.tasks.internal.repositories.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BoardMock {

    private final BoardRepository repository;

    public void load(MockContext ctx) {
        Board tiSP = createBoard("Quadro TI SP", ctx.cityHallSP, ctx.tiSectorsSP);
        Board comprasSP = createBoard("Quadro Compras SP", ctx.cityHallSP, ctx.comprasSectorSP);
        Board financeiroSP = createBoard("Quadro Financeiro SP", ctx.cityHallSP, ctx.financeiroSectorSP);
        Board contratosSP = createBoard("Quadro Contratos SP", ctx.cityHallSP, ctx.contratosSectorSP);
        Board juridicoSP = createBoard("Quadro Juridico SP", ctx.cityHallSP, ctx.juridicoSectorSP);

        Board tiRJ = createBoard("Quadro TI RJ", ctx.cityHallRJ, ctx.tiSectorRJ);
        Board comprasRJ = createBoard("Quadro Compras RJ", ctx.cityHallRJ, ctx.comprasSectorRJ);
        Board financeiroRJ = createBoard("Quadro Financeiro RJ", ctx.cityHallRJ, ctx.financeiroSectorRJ);
        Board contratosRJ = createBoard("Quadro Contratos RJ", ctx.cityHallRJ, ctx.contratosSectorRJ);
        Board juridicoRJ = createBoard("Quadro Juridico RJ", ctx.cityHallRJ, ctx.juridicoSectorRJ);

        repository.saveAll(List.of(
                tiSP,
                comprasSP,
                financeiroSP,
                contratosSP,
                juridicoSP,
                tiRJ,
                comprasRJ,
                financeiroRJ,
                contratosRJ,
                juridicoRJ
        ));

        ctx.tiBoardSP = tiSP;
        ctx.comprasBoardSP = comprasSP;
        ctx.financeiroBoardSP = financeiroSP;
        ctx.contratosBoardSP = contratosSP;
        ctx.juridicoBoardSP = juridicoSP;
        ctx.tiBoardRJ = tiRJ;
        ctx.comprasBoardRJ = comprasRJ;
        ctx.financeiroBoardRJ = financeiroRJ;
        ctx.contratosBoardRJ = contratosRJ;
        ctx.juridicoBoardRJ = juridicoRJ;
    }

    private Board createBoard(String name, com.fiap.hackgov.cityhall_management.internal.entities.CityHall cityHall,
                              com.fiap.hackgov.cityhall_management.internal.entities.Sector sector) {
        Board board = new Board();
        board.setName(name);
        board.setCityHall(cityHall);
        board.setSector(sector);
        return board;
    }
}
