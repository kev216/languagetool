package org.languagetool.rules.pl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

public class MorfologikPolishSpellerRuleTest {

    @Test
    public void testMorfologikSpeller() throws IOException {
        MorfologikPolishSpellerRule rule =
                new MorfologikPolishSpellerRule (TestTools.getMessages("Polish"), Language.POLISH);

        RuleMatch[] matches;
        JLanguageTool langTool = new JLanguageTool(Language.POLISH);


        // correct sentences:
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("To jest test bez jakiegokolwiek błędu.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Żółw na starość wydziela dziwną woń.")).length);
        //test for "LanguageTool":
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("LanguageTool jest świetny!")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);

        //incorrect sentences:

        matches = rule.match(langTool.getAnalyzedSentence("Zolw"));
        // check match positions:
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(4, matches[0].getToPos());
        assertEquals("Zola", matches[0].getSuggestedReplacements().get(0));

        assertEquals(1, rule.match(langTool.getAnalyzedSentence("aõh")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("a")).length);

    }

}