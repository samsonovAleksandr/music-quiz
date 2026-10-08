package com.example.musicquix.bot;

import java.util.List;

/** Text templates for the bot (Telegram HTML parse mode). Kept free of Telegram classes so it is unit-testable. */
final class Messages {

    private Messages() {
    }

    static final String LINE = "━━━━━━━━━━━━━━━━";

    static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static String flag(Language language) {
        return language == Language.RUSSIAN ? "🇷🇺" : "🇬🇧";
    }

    static String welcome(String firstName) {
        return "🎧 <b>Привет, " + esc(firstName) + "!</b>\n\n"
                + "Я показываю четыре строки из песни — ты угадываешь, кто её исполняет.\n"
                + "Песни на русском и английском, только популярные исполнители.\n\n"
                + "Жми <b>«Играть»</b> и поехали! 🚀";
    }

    static String help() {
        return "ℹ️ <b>Как играть</b>\n\n"
                + "1️⃣ Я присылаю отрывок из песни\n"
                + "2️⃣ Ты выбираешь исполнителя из четырёх\n"
                + "3️⃣ Копи серию правильных ответов 🔥\n\n"
                + "<b>Команды</b>\n"
                + "/game — новый вопрос\n"
                + "/lang — язык песен\n"
                + "/stats — статистика";
    }

    static String question(Language language, String lyrics, int streak) {
        StringBuilder sb = new StringBuilder();
        sb.append(flag(language)).append(" <b>Угадай исполнителя</b>");
        if (streak >= 2) {
            sb.append("   🔥 ").append(streak);
        }
        sb.append("\n\n<blockquote>").append(esc(lyrics)).append("</blockquote>");
        return sb.toString();
    }

    /** Question text after the player answered: lyrics, all options with marks and a verdict. */
    static String result(Language language, String lyrics, String title, List<String> options,
                         int chosen, int correct, int streak, int best) {
        StringBuilder sb = new StringBuilder();
        sb.append(flag(language)).append(" <b>Угадай исполнителя</b>\n\n")
                .append("<blockquote>").append(esc(lyrics)).append("</blockquote>\n\n");
        for (int i = 0; i < options.size(); i++) {
            String mark = i == correct ? "✅" : i == chosen ? "❌" : "▫️";
            String name = esc(options.get(i));
            sb.append(mark).append(' ').append(i == correct ? "<b>" + name + "</b>" : name).append('\n');
        }
        sb.append('\n');
        if (chosen == correct) {
            sb.append("🎉 <b>Верно!</b>");
            if (streak >= 2) {
                sb.append("  🔥 Серия: ").append(streak);
            }
        } else {
            sb.append("😔 <b>Мимо.</b>");
            if (best >= 3) {
                sb.append("  Рекорд серии: ").append(best);
            }
        }
        if (title != null && !title.isBlank()) {
            sb.append("\n🎵 «").append(esc(title)).append('»');
        }
        return sb.toString();
    }

    static String stats(int correct, int total, int streak, int best) {
        if (total == 0) {
            return "📊 Пока нет ответов. Начни с /game!";
        }
        int pct = (int) Math.round(100.0 * correct / total);
        return "📊 <b>Твоя статистика</b>\n\n"
                + bar(pct) + "  <b>" + pct + "%</b>\n\n"
                + "✅ Правильно: <b>" + correct + "</b> из " + total + "\n"
                + "🔥 Текущая серия: <b>" + streak + "</b>\n"
                + "🏆 Лучшая серия: <b>" + best + "</b>";
    }

    /** 10-cell progress bar, e.g. 70 -> 🟩🟩🟩🟩🟩🟩🟩⬜⬜⬜ */
    static String bar(int percent) {
        int filled = Math.max(0, Math.min(10, Math.round(percent / 10f)));
        return "🟩".repeat(filled) + "⬜".repeat(10 - filled);
    }

    static String languageChosen(Language language) {
        return flag(language) + " Язык песен: <b>" + language.label() + "</b>\nТеперь можно играть!";
    }

    static String languagePrompt() {
        return "🌐 <b>Выбери язык песен</b>";
    }

    static String noQuestion() {
        return "😕 Не удалось подобрать вопрос. Попробуй ещё раз чуть позже.";
    }
}
