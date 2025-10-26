package org.acme.redis.honra;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.acme.arquivo.ArquivoSolicitacoesService;
import org.acme.models.SolicitacoesHonra;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/honra")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HonraResource {

    @Inject
    ArquivoSolicitacoesService arquivoService;

    private final HonraReactiveService honraReactiveService;

    public HonraResource(HonraReactiveService honraReactiveService) {
        this.honraReactiveService = honraReactiveService;
    }

    /**
     * Salva uma pessoa de forma reativa (define expiração até meia-noite).
     */
    @POST
    @Path("/reactive")
    public Uni<Response> salvarReactive(SolicitacoesHonra honra) {
        return honraReactiveService.salvar(honra)
                .replaceWith(Response.status(Response.Status.CREATED).entity(honra).build());
    }

    @POST
    @Path("/reactive/batch")
    public Uni<Response> salvarVariosReactive() {
        List<SolicitacoesHonra> honra = arquivoService.lerArquivoTxt();

        return honraReactiveService.salvarTodos(Multi.createFrom().items(honra.stream()))
                .replaceWith(Response.status(Response.Status.CREATED).entity(honra).build());
    }

    /**
     * Lista todas as solicitacoes (modo reativo).
     */
    @GET
    @Path("/reactive/lista")
    public Uni<List<SolicitacoesHonra>> listarReactive() {
        return honraReactiveService.listar().collect().asList();
    }

    @GET
    @Path("reactive/keys")
    public Uni<List<String>> listarKeys() {
        return honraReactiveService.keys();
    }

    @GET
    @Path("reactive/duracao")
    public Uni<Long> tempoKey() {
        return honraReactiveService.buscaDuracaokey();
    }

    /**
     * Deleta uma solicitacao específica (modo reativo).
     */
    @DELETE
    @Path("/reactive/{idOperacaoCreditoLivre}")
    public Uni<Response> deletarReactive(@PathParam("idOperacaoCreditoLivre") String idOperacaoCreditoLivre) {
        return honraReactiveService.deletar(idOperacaoCreditoLivre).replaceWith(Response.noContent().build());
    }

    /**
     * Deleta todas as solicitacoes (modo reativo).
     */
    @DELETE
    @Path("/reactive/tudo")
    public Uni<Response> deletarTudoReactive() {
        return honraReactiveService.deletarTudo().replaceWith(Response.noContent().build());
    }
}
