package frc.team5115.subsystems.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.team5115.Constants.VisionConstants;
import frc.team5115.subsystems.drive.Drivetrain;
import java.util.Optional;
import org.littletonrobotics.junction.AutoLogOutput;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;

public class PhotonVision extends SubsystemBase {
    private final Drivetrain drivetrain;
    private final PhotonCamera camera;
    private final AprilTagFieldLayout fieldLayout;
    private final PhotonPoseEstimator poseEstimator;

    public PhotonVision(Drivetrain drivetrain) {
        this.drivetrain = drivetrain;
        camera = new PhotonCamera(VisionConstants.cameraName);
        fieldLayout = AprilTagFields.k2024Crescendo.loadAprilTagLayoutField();
        poseEstimator =
                new PhotonPoseEstimator(
                        fieldLayout,
                        PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
                        camera,
                        VisionConstants.robotToCam);
    }

    @AutoLogOutput
    public Optional<EstimatedRobotPose> getEstimatedGlobalPose(Pose2d prevEstimatedRobotPose) {
        poseEstimator.setReferencePose(prevEstimatedRobotPose);
        return poseEstimator.update();
    }

    @Override
    public void periodic() {
        var option = getEstimatedGlobalPose(drivetrain.getPose());
        if (option.isPresent()) {
            EstimatedRobotPose pose = option.get();
            drivetrain.addVisionMeasurement(pose.estimatedPose.toPose2d(), pose.timestampSeconds);
        }
    }
}
