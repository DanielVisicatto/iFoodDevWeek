package me.dio.Sacola.service.impl;

import lombok.RequiredArgsConstructor;
import me.dio.Sacola.enumeration.FormaPagamento;
import me.dio.Sacola.model.Item;
import me.dio.Sacola.model.Restaurante;
import me.dio.Sacola.model.Sacola;
import me.dio.Sacola.repository.ItemRepository;
import me.dio.Sacola.repository.ProdutoRepository;
import me.dio.Sacola.repository.SacolaRepository;
import me.dio.Sacola.resource.dto.ItemDto;
import me.dio.Sacola.service.SacolaService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.ThreadContext.isEmpty;

@Service
@RequiredArgsConstructor
public class SacolaServiceImpl implements SacolaService {
    private final SacolaRepository sacolaRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemRepository itemRepository;
    @Override
    public Item incluirItemNaSacola(ItemDto itemDto) {
        Sacola sacola = verSacola(itemDto.getSacolaId());

        if(sacola.isFechada()){
            throw new RuntimeException("Esta sacola está fechada.");
        }

        Item itemParaSerInserido =Item.builder()
                .quantidade(itemDto.getQuantidade())
                .sacola(sacola)
                .produto(produtoRepository.findById(itemDto.getProdutoId()).orElseThrow(
                        () -> {
                            throw new RuntimeException("Esse produto não existe.");
                        }
                ))
                .build();

        List<Item> itensDaSacola = sacola.getItens();
        if(itensDaSacola.isEmpty()) {
            itensDaSacola.add(itemParaSerInserido);
        }
        else {
            Restaurante restauranteAtual = itensDaSacola.get(0).getProduto().getRestaurante();
            Restaurante restauranteDoItemParaAdicionar = itemParaSerInserido.getProduto().getRestaurante();
            if(restauranteAtual.equals(restauranteDoItemParaAdicionar)){
                itensDaSacola.add(itemParaSerInserido);
            }
            else {
                throw new RuntimeException("Impossível adicionar produtos de diferentes restaurantes\n" +
                        "termine ou esvazie sua sacola");
            }
        }

        List<Double> valorDosItens = new ArrayList<>();

        for (Item itemDaSacola : itensDaSacola) {
            double  valorTotalItem =
                    itemDaSacola.getProduto().getValorUnitario() * itemDaSacola.getQuantidade();
            valorDosItens.add(valorTotalItem);
        }

        double valorTotalSacola = valorDosItens.stream()
                .mapToDouble(valorTotalDeCadaItem -> valorTotalDeCadaItem)
                .sum();
        //Double valorTotalSacola = 0.0;
        //for(Double valorDeCadaItem : valorDosItens){
        //     valorTotalSacola += valorDeCadaItem;
        //}método acima é mais profissional, além de ser mais simples.
        sacola.setValorTotal(valorTotalSacola);
        sacolaRepository.save(sacola);
        return itemParaSerInserido;
    }

    @Override
    public Sacola verSacola(Long id) {
        return sacolaRepository.findById(id).orElseThrow(
                () -> {
                    throw new RuntimeException("Essa sacola não existe.");
                }
        );
    }

    @Override
    public Sacola fecharSacola(Long id, int numeroFormaPagamento) {
        Sacola sacola = verSacola(id);
        if(sacola.getItens().isEmpty()){
            throw new RuntimeException("Inclua itens na sacola");
        }
        FormaPagamento formaPagamento =
                numeroFormaPagamento == 0 ? FormaPagamento.DINHEIRO : FormaPagamento.MAQUIUNETA;
        sacola.setFormaPagamento(formaPagamento);
        sacola.setFechada(true);
        return sacolaRepository.save(sacola);
    }
}
