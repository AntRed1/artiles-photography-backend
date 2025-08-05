/*
 * The MIT License
 *
 * Copyright 2025 neta1.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.artiles_photography_backend.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author arojas
 *         DTO para solicitudes de creación/actualización de paquetes
 *         fotográficos con imágenes de Cloudinary.
 */
@Data
public class PhotographyPackageSelectRequest {

  private Long id; // Para actualizaciones

  @NotBlank(message = "El publicId es obligatorio")
  private String publicId;

  @NotBlank(message = "El título es obligatorio")
  @Size(max = 100, message = "El título no puede exceder los 100 caracteres")
  private String title;

  @NotBlank(message = "La descripción es obligatoria")
  @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
  private String description;

  @NotNull(message = "El precio es obligatorio")
  @Positive(message = "El precio debe ser mayor que 0")
  private Double price;

  @NotNull(message = "El estado activo es obligatorio")
  private Boolean isActive;

  @NotNull(message = "La visibilidad del precio es obligatoria")
  private Boolean showPrice;

  @NotEmpty(message = "Las características son obligatorias")
  @Size(min = 1, max = 20, message = "Debe haber entre 1 y 20 características")
  private List<@NotBlank(message = "Cada característica debe tener contenido") @Size(max = 200, message = "Cada característica no puede exceder los 200 caracteres") String> features;

}
