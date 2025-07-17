package com.artiles_photography_backend.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author arojas
 */
@Data
public class NotificationRequest {

	@NotNull(message = "Icon is required")
	@Pattern(regexp = "user-plus|image|exclamation-triangle", message = "Invalid icon. Must be one of: user-plus, image, exclamation-triangle")
	private String icon;

	@NotNull(message = "Text is required")
	@NotEmpty(message = "Text cannot be empty")
	@Size(max = 255, message = "Text must be less than 255 characters")
	private String text;

	@NotNull(message = "Color is required")
	@Pattern(regexp = "indigo|green|amber", message = "Invalid color. Must be one of: indigo, green, amber")
	private String color;

	private String link;

}
