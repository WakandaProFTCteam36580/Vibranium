package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import org.rowlandhall.meepmeep.MeepMeep;
import org.rowlandhall.meepmeep.roadrunner.DefaultBotBuilder;
import org.rowlandhall.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

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
            BufferedImage fieldImage = ImageIO.read(imageFile);
            meepMeep.setBackground(fieldImage);
        } catch (IOException e) {
            System.out.println("Não foi possível carregar a imagem. Usando fundo padrão.");
            meepMeep.setBackground(MeepMeep.Background.FIELD_INTOTHEDEEP_JUICE_DARK);
        }

        meepMeep.setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }
}