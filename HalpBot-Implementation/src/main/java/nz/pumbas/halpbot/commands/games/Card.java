/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.commands.games;

import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(chain = false)
public enum Card {
    // Hearts
    HA(11, "<:AH:930707519804936232>"),
    H2(2, "<:2H:930707519481970740>"),
    H3(3, "<:3H:930707519767216158>"),
    H4(4, "<:4H:930707519725244486>"),
    H5(5, "<:5H:930707519716851742>"),
    H6(6, "<:6H:930707519750426694>"),
    H7(7, "<:7H:930707519762993182>"),
    H8(8, "<:8H:930707519855263774>"),
    H9(9, "<:9H:930707519821717546>"),
    H10(10, "<:10H:930707519746224149>"),
    HJ(10, "<:JH:930707519792373800>"),
    HQ(10, "<:QH:930707642878410753>"),
    HK(10, "<:KH:930707519603638313>"),

    // Diamonds
    DA(11, "<:AD:930707519926595654>"),
    D2(2, "<:2D:930707519662358558>"),
    D3(3, "<:3D:930707519653957632>"),
    D4(4, "<:4D:930707519452626945>"),
    D5(5, "<:5D:930707519695908864>"),
    D6(6, "<:6D:930707519758811156>"),
    D7(7, "<:7D:930707519729455144>"),
    D8(8, "<:8D:930707519586832445>"),
    D9(9, "<:9D:930707519729434685>"),
    D10(10, "<:10D:930707519867867137>"),
    DJ(10, "<:JD:930707519783981056>"),
    DQ(10, "<:QD:930707642849062942>"),
    DK(10, "<:KD:930707519771410452>"),

    // Spades
    SA(11, "<:AS:930707519796551700>"),
    S2(2, "<:2S:930707519645564969>"),
    S3(3, "<:3S:930707519679107072>"),
    S4(4, "<:4S:930707519825936384>"),
    S5(5, "<:5S:930707519733633034>"),
    S6(6, "<:6S:930707519712690186>"),
    S7(7, "<:7S:930707519716880424>"),
    S8(8, "<:8S:930707519767203852>"),
    S9(9, "<:9S:930707519825907722>"),
    S10(10, "<:10S:930707519754633256>"),
    SJ(10, "<:JS:930707519859490950>"),
    SQ(10, "<:QS:930707642840678410>"),
    SK(10, "<:KS:930707519528116265>"),

    // Clubs
    CA(11, "<:AC:930707519863660555>"),
    C2(2, "<:2C:930707519725248563>"),
    C3(3, "<:3C:930707519691706388>"),
    C4(4, "<:4C:930707519666552832>"),
    C5(5, "<:5C:930707519683313684>"),
    C6(6, "<:6C:930707519708471327>"),
    C7(7, "<:7C:930707519310016553>"),
    C8(8, "<:8C:930707519729459200>"),
    C9(9, "<:9C:930707519721054238>"),
    C10(10, "<:10C:930707519779794964>"),
    CJ(10, "<:JC:930707519758823434>"),
    CQ(10, "<:QC:930707642538655756>"),
    CK(10, "<:KC:930707519792373840>");

    public static final List<Card> ACES = List.of(HA, DA, SA, CA);
    private static final Random random = new Random();

    @Getter private final int value;
    @Getter private final String emoji;

    Card(int value) {
        this(value, "??");
    }

    Card(int value, String emoji) {
        this.value = value;
        this.emoji = emoji;
    }

    public boolean isAce() {
        return this.value == 11;
    }

    public static Card random() {
        return values()[random.nextInt(values().length)];
    }
}