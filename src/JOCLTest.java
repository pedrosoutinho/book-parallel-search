import org.jocl.*;

public class JOCLTest {
    public static void main(String[] args) {
        // Enable exceptions for JOCL
        CL.setExceptionsEnabled(true);

        // Array to hold the number of OpenCL platforms
        int[] numPlatformsArray = new int[1];

        // Query the number of platforms
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        System.out.println("Number of OpenCL platforms: " + numPlatforms);

        // If platforms exist, retrieve their IDs
        if (numPlatforms > 0) {
            cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
            clGetPlatformIDs(platforms.length, platforms, null);

            for (int i = 0; i < platforms.length; i++) {
                System.out.println("Platform " + i + ": " + platforms[i]);
            }
        } else {
            System.out.println("No OpenCL platforms found.");
        }
    }
}
