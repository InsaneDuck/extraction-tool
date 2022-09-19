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
        if (!(new File(Constants.TEMPORARY_FOLDER).exists() &&
                new File(Constants.TEMPLATE_GENERATION).exists() &&
                new File(Constants.EXTRACTION).exists() &&
                new File(Constants.EXTRACTION_TEMPLATE).exists() &&
                new File(Constants.EXTRACTION_FILES).exists() &&
                new File(Constants.EXTRACTION_MERGED).exists()))
        {
            fail("failed");
        }
    }
}