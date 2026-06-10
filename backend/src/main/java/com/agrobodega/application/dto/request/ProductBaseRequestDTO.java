package com.agrobodega.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProductBaseRequestDTO {

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 ]+$", message = "El nombre solo puede contener letras y números")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    public ProductBaseRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}