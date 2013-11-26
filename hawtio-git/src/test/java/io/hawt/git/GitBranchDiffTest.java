package io.hawt.git;

import io.hawt.util.Strings;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.hawt.git.GitFacadeTest.assertConfigDirectoryExists;
import static io.hawt.git.GitFacadeTest.createTestGitFacade;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests we create a configuration directory
 */
public class GitBranchDiffTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(GitBranchDiffTest.class);

    GitFacade git = createTestGitFacade("git-branch-diff-test");

    public static void main(String[] args) {
        GitBranchDiffTest test = new GitBranchDiffTest();
        try {
            test.init();
            test.testBranchesAndDiff();
            test.destroy();
        } catch (Throwable e) {
            System.out.println("FAILED: " + e);
            e.printStackTrace();
        }
    }
    String authorName = "jstrachan";
    String authorEmail = "james.strachan@gmail.com";

    @Before
    public void init() throws Exception {
        git.setCloneRemoteRepoOnStartup(false);
        git.init();
    }

    @After
    public void destroy() throws Exception {
        git.destroy();
    }

    @Test
    public void testBranchesAndDiff() throws Exception {
        assertConfigDirectoryExists(git);

        // lets do a dummy commit
        String master = "master";
        String branch = "1.0";

        git.write(master, "dummy.txt", "Initial commit", authorName, authorEmail, "hey");

        git.createBranch(master, branch);

        String contentV1 = "Hello world!";
        String contentV2 = "Hello James!";
        String readMePath = "/ReadMe.md";
        String anotherPath = "/Another.md";

        git.write(branch, readMePath, "Initial commit", authorName, authorEmail, contentV1);
        git.write(branch, readMePath, "Updated", authorName, authorEmail, contentV2);

        // force checkout of master branch
        git.read(master, "/");


        // now lets find the versions
        List<CommitInfo> history = git.history(branch, null, readMePath, 0);
        assertSize("history", 2, history);
        for (CommitInfo commitInfo : history) {
            System.out.println("Version: " + commitInfo);
        }

        String id1 = history.get(1).getCommitHashText();
        String id2 = history.get(0).getCommitHashText();
        String version1 = git.getContent(id1, readMePath);
        String version2 = git.getContent(id2, readMePath);
        assertEquals("version1", contentV1, version1);
        assertEquals("version2", contentV2, version2);

        String diff = git.diff(id2, id1, readMePath);
        assertNotNull("diff", diff);
        assertTrue("diff is blank: " + diff, Strings.isNotBlank(diff));
        LOG.info("Diff is: " + diff);
    }

    public static void assertSize(String message, int expectedSize, Collection<?> collection) {
        assertNotNull(message + " is null", collection);
        assertEquals(message + " size when is " + collection, expectedSize, collection.size());
    }

}
