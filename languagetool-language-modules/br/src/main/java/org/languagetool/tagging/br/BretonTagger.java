/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.tagging.br;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

/** Breton Tagger.
 *
 * Based on Breton diction diction from apertium:
 *
 *  Copyright (C) 2008--2010 Francis Tyers <ftyers@prompsit.com>
 *  Copyright (C) 2009--2010 Fulup Jakez <fulup.jakez@ofis-bzh.org>
 *  Copyright (C) 2009       Gwenvael Jekel <jequelg@yahoo.fr>
 *  Development supported by:
 *  * Prompsit Language Engineering, S. L.
 *  * Ofis ar Brezhoneg
 *  * Grup Transducens, Universitat d'Alacant
 *
 * Implemented in FSA.
 *
 * @author Dominique Pellé <dominique.pelle@gmail.com>
 */
public class BretonTagger extends BaseTagger {

  private static final Pattern patternSuffix = Pattern.compile("(?iu)(..+)-(mañ|se|hont)$");
  private Locale conversionLocale = Locale.getDefault();

  @Override
  public final String getFileName() {
    return "/br/breton.dict";
  }

  public BretonTagger() {
    super();
    setLocale(new Locale("br"));
  }

  // This method is almost the same as the 'tag' method in
  // BaseTagger class, except that when probing the
  // dictionary fails, it retry without the suffixes
  // -mañ, -se, -hont.
  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens)
  throws IOException {
    List<AnalyzedToken> taggerTokens;
    List<AnalyzedToken> lowerTaggerTokens;
    List<AnalyzedToken> upperTaggerTokens;
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    final IStemmer dictLookup = new DictionaryLookup(getDictionary());

    Matcher matcher;
    for (String word : sentenceTokens) {
      String probeWord = word;

      // This loop happens when we need to retry probing the dictionary
      // which happens rarely when trying to remove suffixes -mañ, -se, etc.
      for (;;) {
        final List<AnalyzedToken> l = new ArrayList<>();
        final String lowerWord = probeWord.toLowerCase(conversionLocale);
        taggerTokens = asAnalyzedTokenList(word, dictLookup.lookup(probeWord));
        lowerTaggerTokens = asAnalyzedTokenList(word, dictLookup.lookup(lowerWord));
        final boolean isLowercase = probeWord.equals(lowerWord);

        // Normal case.
        addTokens(taggerTokens, l);

        if (!isLowercase) {
          // Lowercase.
          addTokens(lowerTaggerTokens, l);
        }

        // Uppercase.
        if (lowerTaggerTokens.isEmpty() && taggerTokens.isEmpty()) {
          if (isLowercase) {
            upperTaggerTokens = asAnalyzedTokenList(word,
                dictLookup.lookup(StringTools.uppercaseFirstChar(probeWord)));
            if (!upperTaggerTokens.isEmpty()) {
              addTokens(upperTaggerTokens, l);
            }
          }
          if (l.isEmpty()) {
            if ((matcher = patternSuffix.matcher(probeWord)).find()) {
              // Remove the suffix and probe dictionary again.
              // So given a word such as "xxx-mañ", we're going to
              // try to probe the dictionary again with "xxx" this time.
              probeWord = matcher.group(1);
              continue;
            }
            l.add(new AnalyzedToken(word, null, null));
          }
        }
        tokenReadings.add(new AnalyzedTokenReadings(l, pos));
        pos += word.length();
        break;
      }
    }

    return tokenReadings;

  }

  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      for (AnalyzedToken at : taggedTokens) {
        l.add(at);
      }
    }
  }
}
