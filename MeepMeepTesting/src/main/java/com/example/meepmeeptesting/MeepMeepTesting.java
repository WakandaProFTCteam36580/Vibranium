package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class MeepMeepTesting {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                // 1. TAMANHO DO ROBÔ em polegadas: (largura, comprimento)
                .setDimensions(16.7717, 17.7953)
                // 2. RESTRIÇÕES DE MOVIMENTO: (maxVel, maxAccel, maxAngVel, maxAngAccel, trackWidth)
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                // 3. TRAJETÓRIA DO ROBÔ
                .followTrajectorySequence(drive -> drive.trajectorySequenceBuilder(new Pose2d(0, 50, 0))
                        .forward(5)
                        .turn(Math.toRadians(-90))
                        .forward(90)
                        .turn(Math.toRadians(-180))
                        .waitSeconds(5)
                        .turn(Math.toRadians(-90))
                        .forward(25)
                        .turn(Math.toRadians(90))
                        .forward(20)
                        .build());

        // Carregar a imagem personalizada da arena BioBuzz
        try {
            File imageFile = new File("MeepMeepTesting/biobuzz_field.png");
            if (imageFile.exists()) {
                BufferedImage fieldImage = ImageIO.read(imageFile);
                meepMeep.setBackground(fieldImage);
            }
        } catch (IOException e) {
            // Se não encontrar a imagem, o MeepMeep carrega o fundo padrão automaticamente
        }

        meepMeep.setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }
}