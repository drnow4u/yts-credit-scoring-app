package com.yolt.creditscoring.controller.logo;

import com.yolt.creditscoring.service.client.ClientStorageService;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@RestController
@RequiredArgsConstructor
@Validated
public class LogoController {
    private static final String CLIENT_LOGO_ENDPOINT = "/clients/{clientId}/logo";

    private final ClientStorageService clientService;

    @GetMapping(CLIENT_LOGO_ENDPOINT)
    public ResponseEntity<byte[]> getClientLogo(@PathVariable UUID clientId,
                                                @RequestParam(name = "maxWidth", required = false) @Range(min = 32, max = 1024) Integer optionalMaxWidth,
                                                @RequestParam(name = "maxHeight", required = false) @Range(min = 32, max = 1024) Integer optionalMaxHeight) throws IOException {
        // This will become dynamic as part of story YTRN-1291.
        MediaType imageType = MediaType.IMAGE_PNG;

        byte[] clientLogo = clientService.getClientLogo(clientId);
        byte[] scaledClientLogo = scale(clientLogo, imageType, ofNullable(optionalMaxWidth), ofNullable(optionalMaxHeight));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(imageType);
        return new ResponseEntity<>(scaledClientLogo, headers, HttpStatus.OK);
    }

    private byte[] scale(byte[] image, MediaType imageType, Optional<Integer> optionalMaxWidth, Optional<Integer> optionalMaxHeight) throws IOException {
        if (optionalMaxWidth.isEmpty() && optionalMaxHeight.isEmpty()) {
            return image;
        }

        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
        int currentWidth = bufferedImage.getWidth();
        int currentHeight = bufferedImage.getHeight();

        double scalingFactorWidth = optionalMaxWidth.map(maxWidth -> (double) maxWidth / currentWidth).orElse(1.0);
        double scalingFactorHeight = optionalMaxHeight.map(maxHeight -> (double) maxHeight / currentHeight).orElse(1.0);

        double scalingFactor = Math.min(scalingFactorWidth, scalingFactorHeight);
        if (scalingFactor >= 1) { // We want to scale down, not up based on max width/height.
            return image;
        }

        int newWidth = (int) (currentWidth * scalingFactor);
        int newHeight = (int) (currentHeight * scalingFactor);

        Image scaledImage = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        BufferedImage bufferedOutputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        bufferedOutputImage.getGraphics().drawImage(scaledImage, 0, 0, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedOutputImage, imageType.getSubtype(), baos);

        return baos.toByteArray();
    }
}
