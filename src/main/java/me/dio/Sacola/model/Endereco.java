package me.dio.Sacola.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Embeddable
public class Endereco {
    private String cep;
    private String complemento;
}