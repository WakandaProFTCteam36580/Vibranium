package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.drive.AprilTagWebCam;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

@Autonomous(name="Teste AprilTags", group="Autonomous")
public class AutoTags extends LinearOpMode {

    private AprilTagWebCam tagWebCam = new AprilTagWebCam();

    @Override
    public void runOpMode() {
        // Inicializa a câmara passando o hardwareMap e a telemetria
        tagWebCam.init(hardwareMap, telemetry);

        // Otimiza o envio de telemetria para o Driver Hub (evita lag e atualiza fluidamente)
        telemetry.setMsTransmissionInterval(50);

        telemetry.addData("Estado", "Pronto para iniciar");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            tagWebCam.update();

            // Tenta procurar QUALQUER tag entre 0 e 100 para teste
            AprilTagDetection tagTeste = null;
            int idEncontrado = -1; // Variável criada fora do 'for'

            for (int id = 0; id <= 100; id++) {
                tagTeste = tagWebCam.getTagBySpecificId(id);
                if (tagTeste != null) {
                    idEncontrado = id; // Salva o ID encontrado
                    break;
                }
            }

            if (tagTeste != null) {
                telemetry.addData("DETETOU TAG!", "ID lido: " + idEncontrado); // Usa a variável salva
                telemetry.addData("Tem Pose (3D)?", (tagTeste.ftcPose != null) ? "SIM" : "NÃO (Falta calibração/tamanho)");
            } else {
                telemetry.addData("Estado", "Câmara ativa, mas 0 tags visíveis no ecrã.");
            }
            telemetry.update();
        }

        tagWebCam.stop();
    }
}