import dev.insaneduck.extraction_tool.controller.Logic;
import dev.insaneduck.extraction_tool.modal.Constants;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;

class TestClass
{

    @Test
    void setupTempFolder()
    {
        Logic.setupTempFolder();
        File tempDirectory = new File(Constants.TEMPORARY_FOLDER);
        File templateDirectory = new File(Constants.TEMPORARY_FOLDER + "/template");
        File extractionDirectory = new File(Constants.TEMPORARY_FOLDER + "/extraction");
        File extractionTemplateDirectory = new File(Constants.TEMPORARY_FOLDER + "/extraction/template");
        File extractionFilesDirectory = new File(Constants.TEMPORARY_FOLDER + "/extraction/files");
        File extractionMergedDirectory = new File(Constants.TEMPORARY_FOLDER + "/extraction/merged/");

        if (!(tempDirectory.exists() &&
                templateDirectory.exists() &&
                extractionDirectory.exists() &&
                extractionTemplateDirectory.exists() &&
                extractionFilesDirectory.exists() &&
                extractionMergedDirectory.exists()))
        {
            fail("failed");
        }
    }

}