package com.unicesumar;

import com.unicesumar.database.ConexaoSQLite;
import com.unicesumar.entities.Product;
import com.unicesumar.entities.User;
import com.unicesumar.entities.Sale;
import com.unicesumar.paymentMethods.PaymentType;
import com.unicesumar.PaymentManager;
import com.unicesumar.PaymentMethodFactory;
import com.unicesumar.repository.ProductRepository;
import com.unicesumar.repository.UserRepository;
import com.unicesumar.repository.SaleRepository;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws Exception {
        Connection conn = ConexaoSQLite.conectar();

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS products (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "price REAL NOT NULL)"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "email TEXT NOT NULL, " +
                "password TEXT NOT NULL)"
            );
            System.out.println("Tabelas 'products' e 'users' verificadas/criadas com sucesso.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar/verificar tabelas: " + e.getMessage(), e);
        }

        ProductRepository listaDeProdutos = new ProductRepository(conn);
        UserRepository listaDeUsuarios    = new UserRepository(conn);
        SaleRepository saleRepo           = new SaleRepository(conn);
        saleRepo.createTables();

        Scanner scanner = new Scanner(System.in);
        int option;

        do {
            System.out.println("\n--- MENU ---");
            System.out.println("1 - Cadastrar Produto");
            System.out.println("2 - Listar Produtos");
            System.out.println("3 - Cadastrar Usuário");
            System.out.println("4 - Listar Usuários");
            System.out.println("5 - Registrar Venda");
            System.out.println("6 - Sair");
            System.out.print("Escolha uma opção: ");
            option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1:
                    listaDeProdutos.save(new Product("Teste", 10));
                    listaDeProdutos.save(new Product("Computador", 3000));
                    break;
                case 2:
                    listaDeProdutos.findAll().forEach(System.out::println);
                    break;
                case 3:
                    listaDeUsuarios.save(new User("Rafael Labegalini", "rafael@example.com", "1234"));
                    break;
                case 4:
                    listaDeUsuarios.findAll().forEach(System.out::println);
                    break;
                case 5:
                    System.out.print("Digite o Email do usuário: ");
                    String email = scanner.nextLine();
                    Optional<User> optUser = listaDeUsuarios.findAll().stream()
                            .filter(u -> u.getEmail().equals(email))
                            .findFirst();
                    if (!optUser.isPresent()) {
                        System.out.println("Usuário não encontrado.");
                        break;
                    }
                    User user = optUser.get();
                    System.out.println("Usuário encontrado: " + user.getName());

                    System.out.print("Digite os IDs dos produtos (UUIDs separados por vírgula): ");
                    String[] ids = scanner.nextLine().split(",");
                    List<Product> chosen = new ArrayList<>();
                    List<UUID> productIds = new ArrayList<>();
                    for (String idStr : ids) {
                        try {
                            UUID pid = UUID.fromString(idStr.trim());
                            listaDeProdutos.findById(pid).ifPresentOrElse(
                                p -> { chosen.add(p); productIds.add(pid); },
                                () -> System.out.println("Produto não encontrado: " + pid)
                            );
                        } catch (IllegalArgumentException e) {
                            System.out.println("ID inválido: " + idStr);
                        }
                    }
                    if (chosen.isEmpty()) break;

                    double total = chosen.stream().mapToDouble(Product::getPrice).sum();
                    System.out.printf("Total: R$ %.2f%n", total);

                    System.out.println("Escolha a forma de pagamento:");
                    System.out.println("1 - CARTAO\n2 - BOLETO\n3 - PIX");
                    int payOpt = Integer.parseInt(scanner.nextLine());

                    PaymentType pt;
                    switch (payOpt) {
                        case 1: pt = PaymentType.CARTAO; break;
                        case 2: pt = PaymentType.BOLETO; break;
                        case 3: pt = PaymentType.PIX;    break;
                        default:
                            System.out.println("Opção de pagamento inválida.");
                            continue;
                    }

                    PaymentManager pm = new PaymentManager();
                    pm.setPaymentMethod(PaymentMethodFactory.create(pt));
                    System.out.println("Aguarde, efetuando pagamento...");
                    pm.pay(total);
                    System.out.println("Chave de Autenticação: " + UUID.randomUUID());

                    Sale sale = new Sale(user.getUuid(), productIds, total, pt);
                    saleRepo.save(sale);
                    System.out.println("Venda registrada com sucesso!");
                    break;
                case 6:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (option != 6);

        scanner.close();
        conn.close();
    }
}