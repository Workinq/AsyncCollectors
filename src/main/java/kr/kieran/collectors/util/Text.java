/*
 * This file is part of AsyncCollectors, licensed under the MIT License.
 *
 * Copyright (c) Workinq (Kieraaaan) <kieran@kieraaan.me>
 * Copyright (c) contributors
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

package kr.kieran.collectors.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Text
{

    // TODO: Make methods to convert from String to Component

    public static String color(String text)
    {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> uncolored)
    {
        List<String> colored = new ArrayList<>();
        for (String text : uncolored)
        {
            colored.add(color(text));
        }
        return colored;
    }

    public static void message(CommandSender player, String message, Object... args)
    {
        if (message != null && !message.isEmpty()) player.sendMessage(color(String.format(message, args)));
    }

    private static String implode(Object[] list)
    {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < list.length; i++)
        {
            Object item = list[i];
            String str = (item == null ? "NULL" : item.toString());

            if (i != 0) ret.append(" ");
            ret.append(str);
        }
        return ret.toString();
    }

    private static final Pattern PATTERN_ENUM_SPLIT = Pattern.compile("[\\s_]+");
    public static String getNicedEnumString(String name)
    {
        List<String> parts = new ArrayList<>();
        for (String part : PATTERN_ENUM_SPLIT.split(name.toLowerCase()))
        {
            parts.add(part.substring(0, 1).toUpperCase() + part.substring(1));
        }
        return ChatColor.WHITE + implode(parts.toArray(new Object[0]));
    }

}
