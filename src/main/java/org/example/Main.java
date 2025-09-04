package org.example;

import org.example.Model.*;
import org.example.dao.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner input = new Scanner(System.in);

    private static final ClienteDAO clienteDAO = new ClienteDAO();
    private static final MotoristaDAO motoristaDAO = new MotoristaDAO();
    private static final PedidoDAO pedidoDAO = new PedidoDAO();
    private static final EntregaDAO entregaDAO = new EntregaDAO();
    private static final HistoricoEntregaDAO historicoDAO = new HistoricoEntregaDAO();

    public static void main(String[] args) {
        while (true) {
            exibirMenu();
            int op = lerInt("Opção: ");
            try {
                switch (op) {
                    case 1 -> cadastrarCliente();
                    case 2 -> cadastrarMotorista();
                    case 3 -> criarPedido();
                    case 4 -> atribuirPedidoAMotorista();
                    case 5 -> registrarEventoEntrega();
                    case 6 -> atualizarStatusEntrega();
                    case 7 -> listarTodasEntregas();
                    case 8 -> relTotalEntregasPorMotorista();
                    case 9 -> relClientesMaiorVolume();
                    case 10 -> relPedidosPendentesPorEstado();
                    case 11 -> relEntregasAtrasadasPorCidade();
                    case 12 -> buscarPedidosPorCpfCnpj();
                    case 0 -> { System.out.println("Saindo..."); return; }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (SQLException e) {
                System.out.println("Erro de banco: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println();
        }
    }

    private static void exibirMenu() {
        System.out.println("=========================================");
        System.out.println("1 - Cadastrar Cliente");
        System.out.println("2 - Cadastrar Motorista");
        System.out.println("3 - Criar Pedido");
        System.out.println("4 - Atribuir Pedido a Motorista (Gerar Entrega)");
        System.out.println("5 - Registrar Evento de Entrega (Histórico)");
        System.out.println("6 - Atualizar Status da Entrega");
        System.out.println("7 - Listar Todas as Entregas com Cliente e Motorista");
        System.out.println("8 - Relatório: Total de Entregas por Motorista");
        System.out.println("9 - Relatório: Clientes com Maior Volume Entregue");
        System.out.println("10 - Relatório: Pedidos Pendentes por Estado");
        System.out.println("11 - Relatório: Entregas Atrasadas por Cidade");
        System.out.println("12 - Buscar Pedido por CPF/CNPJ do Cliente");
        System.out.println("0 - Sair");
        System.out.println("=========================================");
    }

    /* helpers de leitura */
    private static String lerString(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }
    private static int lerInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String l = input.nextLine().trim();
                return Integer.parseInt(l);
            } catch (NumberFormatException e) {
                System.out.println("Número inválido, tente novamente.");
            }
        }
    }
    private static double lerDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String l = input.nextLine().trim();
                return Double.parseDouble(l);
            } catch (NumberFormatException e) {
                System.out.println("Número inválido, tente novamente.");
            }
        }
    }

    /* funcionalidades */
    private static void cadastrarCliente() throws SQLException {
        System.out.println("--- Cadastrar Cliente ---");
        String nome = lerString("Nome: ");
        String cpf = lerString("CPF/CNPJ: ");
        String end = lerString("Endereço: ");
        String cidade = lerString("Cidade: ");
        String estado = lerString("Estado (UF): ");
        Cliente c = new Cliente(nome, cpf, end, cidade, estado);
        clienteDAO.inserirCliente(c);
        System.out.println("Cliente cadastrado.");
    }

    private static void cadastrarMotorista() throws SQLException {
        System.out.println("--- Cadastrar Motorista ---");
        String nome = lerString("Nome: ");
        String cnh = lerString("CNH: ");
        String veic = lerString("Veículo: ");
        String cidadeBase = lerString("Cidade base: ");
        Motorista m = new Motorista(nome, cnh, veic, cidadeBase);
        motoristaDAO.inserirMotorista(m);
        System.out.println("Motorista cadastrado.");
    }

    private static void criarPedido() throws SQLException {
        System.out.println("--- Criar Pedido ---");
        int clienteId = lerInt("ID do Cliente: ");
        Cliente c = clienteDAO.buscarPorId(clienteId);
        if (c == null) { System.out.println("Cliente não encontrado."); return; }
        String dataStr = lerString("Data (YYYY-MM-DD) [enter=hoje]: ");
        LocalDate d = dataStr.isBlank() ? LocalDate.now() : LocalDate.parse(dataStr);
        double vol = lerDouble("Volume (m3): ");
        double peso = lerDouble("Peso (kg): ");
        Pedido p = new Pedido(clienteId, d, vol, peso, "PENDENTE");
        pedidoDAO.inserirPedido(p);
        System.out.println("Pedido criado.");
    }

    private static void atribuirPedidoAMotorista() throws SQLException {
        System.out.println("--- Atribuir Pedido ---");
        int pedidoId = lerInt("ID do Pedido: ");
        Pedido p = pedidoDAO.buscarPorId(pedidoId);
        if (p == null) { System.out.println("Pedido não encontrado."); return; }
        if (!"PENDENTE".equalsIgnoreCase(p.getStatus())) { System.out.println("Somente pedidos PENDENTE podem ser atribuídos."); return; }
        int motoristaId = lerInt("ID do Motorista: ");
        Motorista m = motoristaDAO.buscarPorId(motoristaId);
        if (m == null) { System.out.println("Motorista não encontrado."); return; }
        Entrega e = new Entrega();
        e.setPedidoId(pedidoId);
        e.setMotoristaId(motoristaId);
        e.setDataSaida(LocalDateTime.now());
        e.setStatus("EM_ROTA");
        entregaDAO.criarEntrega(e);
        pedidoDAO.atualizarStatus(pedidoId, "EM_TRANSPORTE");
        System.out.println("Entrega gerada e atribuída.");
    }

    private static void registrarEventoEntrega() throws SQLException {
        System.out.println("--- Registrar Evento ---");
        int entregaId = lerInt("ID da Entrega: ");
        Entrega e = entregaDAO.buscarPorId(entregaId);
        if (e == null) { System.out.println("Entrega não encontrada."); return; }
        String desc = lerString("Descrição do evento: ");
        HistoricoEntrega h = new HistoricoEntrega();
        h.setEntregaId(entregaId);
        h.setDataEvento(LocalDateTime.now());
        h.setDescricao(desc);
        historicoDAO.salvar(h);
        System.out.println("Evento registrado.");
    }

    private static void atualizarStatusEntrega() throws SQLException {
        System.out.println("--- Atualizar Status da Entrega ---");
        int entregaId = lerInt("ID da Entrega: ");
        Entrega e = entregaDAO.buscarPorId(entregaId);
        if (e == null) { System.out.println("Entrega não encontrada."); return; }
        System.out.println("Status atual: " + e.getStatus());
        String novo = lerString("Novo status (EM_ROTA, ENTREGUE, ATRASADA): ").toUpperCase();
        e.setStatus(novo);
        if ("ENTREGUE".equalsIgnoreCase(novo)) {
            e.setDataEntrega(LocalDateTime.now());
            // atualizar pedido também
            Entrega entFromDb = entregaDAO.buscarPorId(entregaId);
            if (entFromDb != null) {
                pedidoDAO.atualizarStatus(entFromDb.getPedidoId(), "ENTREGUE");
            }
        }
        entregaDAO.atualizarEntrega(e);
        System.out.println("Status atualizado.");
    }

    private static void listarTodasEntregas() throws SQLException {
        System.out.println("--- Entregas ---");
        List<Entrega> list = entregaDAO.listarEntregasComDetalhes();
        if (list.isEmpty()) { System.out.println("Nenhuma entrega."); return; }
        for (Entrega e : list) {
            System.out.printf("ID:%d | Pedido:%d | Cliente:%s | Motorista:%s | Status:%s | Saída:%s | Entrega:%s%n",
                    e.getId(), e.getPedidoId(),
                    e.getClienteNome() == null ? "-" : e.getClienteNome(),
                    e.getMotoristaNome() == null ? "-" : e.getMotoristaNome(),
                    e.getStatus(),
                    e.getDataSaida() == null ? "-" : e.getDataSaida(),
                    e.getDataEntrega() == null ? "-" : e.getDataEntrega()
            );
        }
    }

    private static void relTotalEntregasPorMotorista() throws SQLException {
        System.out.println("--- Total de Entregas por Motorista ---");
        List<EntregaDAO.MotoristaCount> list = entregaDAO.relatorioTotalEntregasPorMotorista();
        for (var r : list) {
            System.out.printf("%s (ID %d) -> %d entregas%n", r.getNome(), r.getMotoristaId(), r.getTotal());
        }
    }

    private static void relClientesMaiorVolume() throws SQLException {
        System.out.println("--- Clientes por volume entregue ---");
        List<PedidoDAO.ClienteVolume> list = pedidoDAO.relatorioClientesPorVolume();
        for (var r : list) {
            System.out.printf("%s (ID %d) -> %.3f m3%n", r.getNomeCliente(), r.getClienteId(), r.getVolumeTotal());
        }
    }

    private static void relPedidosPendentesPorEstado() throws SQLException {
        System.out.println("--- Pedidos pendentes por estado ---");
        List<PedidoDAO.EstadoCount> list = pedidoDAO.relatorioPedidosPendentesPorEstado();
        for (var r : list) {
            System.out.printf("%s -> %d pendentes%n", r.getEstado(), r.getTotal());
        }
    }

    private static void relEntregasAtrasadasPorCidade() throws SQLException {
        System.out.println("--- Entregas atrasadas por cidade ---");
        List<EntregaDAO.CidadeCount> list = entregaDAO.relatorioEntregasAtrasadasPorCidade();
        for (var r : list) {
            System.out.printf("%s -> %d atrasadas%n", r.getCidade(), r.getTotal());
        }
    }

    private static void buscarPedidosPorCpfCnpj() throws SQLException {
        System.out.println("--- Buscar pedidos por CPF/CNPJ ---");
        String cpf = lerString("CPF/CNPJ: ");
        List<Pedido> list = pedidoDAO.buscarPorCpfCnpj(cpf);
        if (list.isEmpty()) { System.out.println("Nenhum pedido."); return; }
        for (Pedido p : list) {
            System.out.printf("ID:%d | ClienteID:%d | Data:%s | Volume:%.3f | Peso:%.3f | Status:%s%n",
                    p.getId(), p.getClienteId(), p.getDataPedido(), p.getVolumeM3(), p.getPesoKg(), p.getStatus());
        }
    }
}
