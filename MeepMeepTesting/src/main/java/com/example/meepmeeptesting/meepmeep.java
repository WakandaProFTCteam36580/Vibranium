package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class meepmeep {

    // =====================================================================
    // COORDENADAS (polegadas / graus) calibradas pela sua imagem da arena,
    // aliança VERMELHA. Na sua imagem o campo vai de x = -36 a +35 e
    // y = -49 a +53. Colmeias vermelhas: de baixo ~(-7, -6) e de cima ~(-7, 15).
    // =====================================================================
    // Largada no canto inferior esquerdo, de frente para a colmeia vermelha de baixo
    static final double START_DEG = 72;
    static final Pose2d START    = new Pose2d(-18, -38.5, Math.toRadians(START_DEG));
    // Ida reta (heading 180) pela faixa de baixo até o pólen do canto inferior esquerdo
    static final double WALL_DIST = 8;
    // Faixa junto à parede esquerda (para subir sem bater nas colmeias)
    static final Pose2d LANE     = new Pose2d(-27, -38.5, Math.toRadians(90));
    static final Pose2d LANE_TOP = new Pose2d(-27, 42, Math.toRadians(90));
    // Flower (círculo rosa): pólen na parede de cima, em ~(-12.6, 51)
    static final Pose2d FLOWER   = new Pose2d(-12.6, 42, Math.toRadians(90));
    // Direção da colmeia vermelha de cima vista da flower
    static final double HIVE_DEG_AT_FLOWER = -79;
    // Estacionamento: retângulo vermelho ao lado do tubo laranja
    static final Pose2d PARK     = new Pose2d(-26, 28, Math.toRadians(90));

    // Tempos parados (s)
    static final double SHOOT_1_SEC       = 4.0; // 1º lançamento na largada
    static final double SHOOT_2_SEC       = 3.0; // lançamento após voltar da parede
    static final double FLOWER_SHOOT_SEC  = 2.5; // lança na colmeia ao chegar na flower
    static final double COLLECT_SEC       = 2.0; // pega os polens na flower
    static final double FLOWER_SHOOT2_SEC = 2.5; // lança de novo na colmeia

    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);

        final double toHive   = Math.toRadians(HIVE_DEG_AT_FLOWER - 90); // flower -> colmeia
        final double toFlower = Math.toRadians(90 - HIVE_DEG_AT_FLOWER); // colmeia -> flower

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                // TAMANHO DO ROBÔ em polegadas: (largura, comprimento)
                .setDimensions(16.7717, 17.7953)
                // (maxVel, maxAccel, maxAngVel, maxAngAccel, trackWidth)
                .setConstraints(40, 40, Math.toRadians(180), Math.toRadians(180), 15)
                .followTrajectorySequence(drive -> drive.trajectorySequenceBuilder(START)

                        // 1) De frente para a colmeia: lança
                        .waitSeconds(SHOOT_1_SEC)

                        // 2) Vira para a esquerda (até ficar de frente para a parede, heading 180),
                        //    vai em linha reta pegar o pólen do canto e volta à posição inicial
                        .turn(Math.toRadians(180 - START_DEG))
                        .forward(WALL_DIST)
                        .back(WALL_DIST)
                        .turn(Math.toRadians(START_DEG - 180))

                        // 3) Lança novamente
                        .waitSeconds(SHOOT_2_SEC)

                        // 4) Vai para a esquerda e sobe até a flower (círculo rosa)
                        .lineToLinearHeading(LANE)
                        .lineToLinearHeading(LANE_TOP)
                        .lineToLinearHeading(FLOWER)

                        // 5) Alinha com a colmeia e lança
                        .turn(toHive)
                        .waitSeconds(FLOWER_SHOOT_SEC)

                        // 6) Vira para a flower e pega os polens
                        .turn(toFlower)
                        .waitSeconds(COLLECT_SEC)

                        // 7) Vira para a colmeia de novo e lança
                        .turn(toHive)
                        .waitSeconds(FLOWER_SHOOT2_SEC)

                        // 8) Estaciona
                        .lineToLinearHeading(PARK)
                        .waitSeconds(1)
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