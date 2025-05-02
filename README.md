# English Learning Bot

Бот для изучения английских слов.
Слова размещаются в words.txt, в формате: английское слово|перевод|0.
Каждая строка соответствует изучаемому слову.

При запуске бота новым пользователем, файл words.txt копируется с именем `id_чата_пользователя.txt`

## Публикация

Для публикации бота используем VPS

### Настройки VPS

1. Создаём виртуальный сервер (Ubuntu), получаем ip-адрес и пароль для root
2. Подключаемся к серверу по ssh: `ssh root@<ip_address>`
3. Обновляем пакеты: `apt update`, `apt upgrade`
4. Устанавливаем JDK: `apt install default-jdk`
5. Проверяем наличие JDK: `java --version`

### Публикация и запуск

1. Собираем shadowJar командой `./gradlew shadowJar`
2. Наш shadowJar копируем на VPS и переименовываем: `scp build/libs/KotlinTelegramBot-1.0-SNAPSHOT-all.jar root@<ip_address>:/root/bot.jar`
3. Также копируем words.txt: `scp words.txt root@<ip_address>:/root/words.txt`
4. Запускаем бота в фоне командой: `nohup java -jar bot.jar <BOT_TOKEN> &`
5. Проверяем работу бота