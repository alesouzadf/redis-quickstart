package org.acme.redis.pessoa;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

import org.acme.arquivo.ArquivoPessoaService;
import org.acme.models.Pessoa;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/pessoas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PessoaResource {

    @Inject
    ArquivoPessoaService arquivoService;

    private final PessoaService pessoaService;
    private final PessoaReactiveService pessoaReactiveService;

    public PessoaResource(PessoaService pessoaService, PessoaReactiveService pessoaReactiveService) {
        this.pessoaService = pessoaService;
        this.pessoaReactiveService = pessoaReactiveService;
    }

    // ============================================================
    // ======= MÉTODOS SÍNCRONOS (RedisDataSource) ===============
    // ============================================================

    /**
     * Salva uma pessoa (modo síncrono) e define expiração até meia-noite.
     */
    @POST
    public Response salvar(Pessoa pessoa) {
        pessoaService.salvar(pessoa);
        return Response.status(Response.Status.CREATED).entity(pessoa).build();
    }

    /**
     * Lista todas as pessoas salvas (modo síncrono).
     */
    @GET
    public List<Pessoa> listar() {
        return pessoaService.listar();
    }

    /**
     * Busca uma pessoa específica pelo nome (modo síncrono).
     */
    @GET
    @Path("/{nome}")
    public Pessoa buscar(@PathParam("nome") String nome) {
        Pessoa pessoa = pessoaService.buscar(nome);
        if (pessoa == null) {
            throw new NotFoundException("Pessoa não encontrada: " + nome);
        }
        return pessoa;
    }

    /**
     * Deleta uma pessoa específica (modo síncrono).
     */
    @DELETE
    @Path("/{nome}")
    public Response deletar(@PathParam("nome") String nome) {
        pessoaService.deletar(nome);
        return Response.noContent().build();
    }

    /**
     * Deleta todas as pessoas (modo síncrono).
     */
    @DELETE
    @Path("/tudo")
    public Response deletarTudo() {
        pessoaService.deletarTudo();
        return Response.noContent().build();
    }

    // ============================================================
    // ======= MÉTODOS REATIVOS (ReactiveRedisDataSource) =========
    // ============================================================

    /**
     * Salva uma pessoa de forma reativa (define expiração até meia-noite).
     */
    @POST
    @Path("/reactive")
    public Uni<Response> salvarReactive(Pessoa pessoa) {
        return pessoaReactiveService.salvar(pessoa)
                .replaceWith(Response.status(Response.Status.CREATED).entity(pessoa).build());
    }

    @POST
    @Path("/reactive/batch")
    public Uni<Response> salvarVariosReactive() {
        List<Pessoa> pessoas = arquivoService.lerPessoasDoArquivo();
        return pessoaReactiveService.salvarTodos(Multi.createFrom().items(pessoas.stream()))
                .replaceWith(Response.status(Response.Status.CREATED).entity(pessoas).build());
    }

    /**
     * Lista todas as pessoas (modo reativo).
     */
    @GET
    @Path("/reactive")
    public Uni<List<Pessoa>> listarReactive() {
        return pessoaReactiveService.listar().collect().asList();
    }

    /**
     * Busca uma pessoa específica (modo reativo).
     */
    @GET
    @Path("/reactive/{nome}")
    public Uni<Response> buscarReactive(@PathParam("nome") String nome) {
        return pessoaReactiveService.buscar(nome)
                .onItem().ifNotNull().transform(p -> Response.ok(p).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("reactive/keys")
    public Uni<List<String>> listarKeys() {
        return pessoaReactiveService.keys();
    }

    @GET
    @Path("reactive/duracao")
    public Uni<Long> tempoKey() {
        return pessoaReactiveService.buscaDuracaokey();
    }

    @PUT
    @Path("/reactive/{id}")
    public Uni<Response> atualizarCampoReactive(@PathParam("id") String id,
            Pessoa pessoa) {
        return pessoaReactiveService.atualizar(id, pessoa.getNome(), pessoa.getIdade())
                .replaceWith(Response.noContent().build());
    }

    /**
     * Deleta uma pessoa específica (modo reativo).
     */
    @DELETE
    @Path("/reactive/{nome}")
    public Uni<Response> deletarReactive(@PathParam("nome") String nome) {
        return pessoaReactiveService.deletar(nome).replaceWith(Response.noContent().build());
    }

    /**
     * Deleta todas as pessoas (modo reativo).
     */
    @DELETE
    @Path("/reactive/tudo")
    public Uni<Response> deletarTudoReactive() {
        return pessoaReactiveService.deletarTudo().replaceWith(Response.noContent().build());
    }
}
