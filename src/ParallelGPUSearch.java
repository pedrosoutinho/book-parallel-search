import org.jocl.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.jocl.CL.*;

public class ParallelGPUSearch implements WordSearch {

    private static final int MAX_LINES = 1024; // Maximum lines to process in one batch
    private static final int MAX_LINE_LENGTH = 1024; // Maximum length of a line

    @Override
    public int countOccurrences(String filePath, String word) throws IOException {
        // Read the file into a list of lines
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        // Prepare OpenCL
        CL.setExceptionsEnabled(true);

        // Obtain the OpenCL platform and device
        cl_platform_id[] platforms = new cl_platform_id[1];
        clGetPlatformIDs(1, platforms, null);
        cl_platform_id platform = platforms[0];

        cl_device_id[] devices = new cl_device_id[1];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 1, devices, null);
        cl_device_id device = devices[0];

        cl_context context = clCreateContext(null, 1, devices, null, null, null);
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Prepare the kernel source code
        String kernelSource = loadKernelSource();

        // Create the program and kernel
        cl_program program = clCreateProgramWithSource(context, 1, new String[] { kernelSource }, null, null);
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kernel = clCreateKernel(program, "count_word_occurrences", null);

        // Prepare the word and line buffers
        byte[] wordBytes = word.getBytes(StandardCharsets.UTF_8);
        int wordLength = wordBytes.length;

        cl_mem wordBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, wordBytes.length,
                Pointer.to(wordBytes), null);

        // Result buffer
        int[] results = new int[lines.size()];
        cl_mem resultBuffer = clCreateBuffer(context, CL_MEM_WRITE_ONLY, results.length * Sizeof.cl_int, null, null);

        // Process each line in the file
        int totalCount = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);

            cl_mem lineBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, lineBytes.length,
                    Pointer.to(lineBytes), null);

            // Set kernel arguments
            clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(lineBuffer));
            clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[] { lineBytes.length }));
            clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(wordBuffer));
            clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[] { wordLength }));
            clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(resultBuffer));

            // Execute kernel
            long[] globalWorkSize = new long[] { lineBytes.length };
            clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, globalWorkSize, null, 0, null, null);

            // Read results
            clEnqueueReadBuffer(commandQueue, resultBuffer, CL_TRUE, 0, results.length * Sizeof.cl_int,
                    Pointer.to(results), 0, null, null);

            // Add to total count
            totalCount += results[0];

            // Release line buffer
            clReleaseMemObject(lineBuffer);
        }

        // Cleanup
        clReleaseMemObject(wordBuffer);
        clReleaseMemObject(resultBuffer);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        return totalCount;
    }

    private String loadKernelSource() {
        return "__kernel void count_word_occurrences("
                + "__global const char *line,"
                + "int lineLength,"
                + "__global const char *word,"
                + "int wordLength,"
                + "__global int *result) {"
                + "    int count = 0;"
                + "    for (int i = 0; i <= lineLength - wordLength; i++) {"
                + "        int match = 1;"
                + "        for (int j = 0; j < wordLength; j++) {"
                + "            if (line[i + j] != word[j]) {"
                + "                match = 0;"
                + "                break;"
                + "            }"
                + "        }"
                + "        if (match == 1) {"
                + "            count++;"
                + "        }"
                + "    }"
                + "    result[0] = count;"
                + "}";
    }

}
