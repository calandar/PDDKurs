package com.example.kurs2

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `UserAnswer` (`ticketId` INTEGER NOT NULL, `questionId` INTEGER NOT NULL, `isCorrect` INTEGER NOT NULL, PRIMARY KEY(`ticketId`, `questionId`), FOREIGN KEY(`ticketId`) REFERENCES `tickets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`questionId`) REFERENCES `questions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE UserAnswer ADD COLUMN userAnswer TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tickets ADD COLUMN isMarathon INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tickets ADD COLUMN theme TEXT")
    }
}

@Database(entities = [Question::class, Ticket::class, UserAnswer::class], version = 5)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun ticketDao(): TicketDao
    abstract fun userAnswerDao(): UserAnswerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // Добавляем новую миграцию
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}


object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null
    private val basepath: String = "app/src/main/res/drawable/"

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // Добавляем миграцию
                .build()
            INSTANCE = instance
            instance
        }
    }

    fun imageTo(basepath: String, name: String?): String? {
        return if (name != null) {
            basepath + name
        } else {
            null
        }
    }

    suspend fun createMarathonTicket(context: Context): Int {
        val db = getDatabase(context)
        val questionDao = db.questionDao()
        val ticketDao = db.ticketDao()

        // Проверяем, существует ли уже марафонный билет
        val existingMarathonTicket = ticketDao.getMarathonTicket()
        if (existingMarathonTicket != null) {
            return existingMarathonTicket.id
        }

        val allQuestions = questionDao.getAllQuestions()
        if (allQuestions.isEmpty()) {
            throw IllegalStateException("No questions found in the database")
        }
        val shuffledQuestions = allQuestions.shuffled()
        val marathonTicket = Ticket(questions = shuffledQuestions.map { it.id }, isMarathon = true)

        val ticketId = ticketDao.insertMarathonTicket(marathonTicket)
        return ticketId.toInt()
    }

    suspend fun deleteMarathonTicket(context: Context, ticketId: Int) {
        val db = getDatabase(context)
        val ticketDao = db.ticketDao()
        ticketDao.deleteTicketById(ticketId)
    }

    suspend fun getTicketById(context: Context, ticketId: Int): Ticket? {
        val db = getDatabase(context)
        val ticketDao = db.ticketDao()
        return ticketDao.getTicketById(ticketId)
    }

    suspend fun createThemeTicket(context: Context, theme: String): Int {
        val db = getDatabase(context)
        val questionDao = db.questionDao()
        val ticketDao = db.ticketDao()

        // Проверяем, существует ли уже билет для этой темы
        val existingThemeTicket = ticketDao.getTicketByTheme(theme)
        if (existingThemeTicket != null) {
            return existingThemeTicket.id
        }

        val questions = questionDao.getQuestionsByTheme(theme)
        if (questions.isEmpty()) {
            throw IllegalStateException("No questions found for the theme: $theme")
        }
        val shuffledQuestions = questions.shuffled()
        val themeTicket = Ticket(questions = shuffledQuestions.map { it.id }, isMarathon = false, theme = theme)

        val ticketId = ticketDao.insertThemeTicket(themeTicket)
        return ticketId.toInt()
    }


    suspend fun addInitialQuestionsIfNotExist(context: Context) {
        val db = getDatabase(context)
        val questionDao = db.questionDao()
        val questionsToAdd = listOf(
            Question(theme = "Общие вопросы", question = "Что означает термин «Ограниченная видимость»?", options = listOf("Видимость водителем дороги, ограниченная рельефом местности, геометрическими параметрами дороги, растительностью, строениями, сооружениями или другими объектами.", "Видимость водителем дороги менее 300 м в условиях тумана, дождя, снегопада, а также в сумерки.", "Видимость водителем дороги менее 150 м в ночное время.", "Видимость водителем дороги во всех перечисленных случаях."), correctAnswer = "Видимость водителем дороги, ограниченная рельефом местности, геометрическими параметрами дороги, растительностью, строениями, сооружениями или другими объектами."),
            Question(theme = "Предупреждающие знаки", question = "Какие из указанных знаков распространяют свое действие только на период времени, когда покрытие проезжей части влажное?", options = listOf("Только А.", "А и Б.", "Все."), correctAnswer = "Только А."),
            Question(theme = "Запрещающие знаки", question = "Разрешается ли Вам поставить автомобиль на стоянку в указанном месте?", options = listOf("Разрешается.", "Разрешается, если Вы проживаете рядом с этим местом.", "Запрещается."), correctAnswer = "Запрещается."),
            Question(theme = "Запрещающие знаки", question = "Вы управляете грузовым автомобилем с разрешенной максимальной массой не более 3,5 т. В каком направлении Вам разрешено дальнейшее движение?", options = listOf("Только направо.", "Направо, налево и в обратном направлении.", "В любом."), correctAnswer = "В любом.", hint = "Установленный за перекрестком знак 3.4  «Движение грузовых автомобилей запрещено» (без указания массы на знаке) запрещает движение в прямом направлении только грузовым автомобилям с разрешенной максимальной массой более 3,5 т, а знак 6.15.2  «Направление движения для грузовых автомобилей» перед перекрестком указывает для таких автомобилей рекомендуемое направление движения для объезда закрытого для них участка дороги. Таким образом, Вы на этом перекрестке можете двигаться в любом направлении.", imageUrl = "id4"),
            Question(
                theme = "Вертикальная разметка",
                question = "Такой вертикальной разметкой обозначают:",
                options = listOf("Все вертикальные элементы дорожных сооружений.", "Только вертикальные элементы дорожных сооружений, представляющие опасность для движущихся транспортных средств.", "Только вертикальные элементы дорожных сооружений на автомагистралях и дорогах, обозначенных знаком 5.3 «Дорога для автомобилей»."),
                correctAnswer = "Только вертикальные элементы дорожных сооружений, представляющие опасность для движущихся транспортных средств.",
                hint = "Разметкой 2.1.1  и разметкой 2.1.3  обозначают вертикальные элементы дорожных сооружений (опоры мостов, путепроводов, торцовые части парапетов и т. п.), когда эти элементы представляют опасность для движущихся ТС.",
                imageUrl = "id5"
            ), //5
            Question(
                theme = "Применение специальных сигналов",
                question = "Преимущество перед другими участниками движения имеет водитель автомобиля:",
                options = listOf("Только с включенным проблесковым маячком синего или бело-лунного цвета.", "Только с включенным проблесковым маячком оранжевого или желтого цвета.", "Только с включенными проблесковым маячком синего (синего и красного) цвета и специальным звуковым сигналом.", "Любого из перечисленных."),
                correctAnswer = "Только с включенными проблесковым маячком синего (синего и красного) цвета и специальным звуковым сигналом.",
                hint = "Водитель автомобиля имеет преимущество перед другими участниками движения только в том случае, если на автомобиле включены проблесковый маячок синего (или синего и красного) цвета и специальный звуковой сигнал .",
            ), // 6
            Question(
                theme = "Проезд перекрестков",
                question = "Вы намерены остановиться сразу за перекрестком. В каком месте необходимо включить указатели правого поворота?",
                options = listOf("До въезда на перекресток, чтобы заблаговременно предупредить других водителей об остановке.", "Только после въезда на перекресток.", "Место включения указателей поворота не имеет значения, так как поворот направо запрещен."),
                correctAnswer = "Только после въезда на перекресток.",
                hint = "В данной ситуации включение правых указателей поворота до въезда на перекресток может быть воспринято водителем легкового автомобиля как ваше решение повернуть направо на перекрестке и послужить ему сигналом к началу движения, что создаст аварийную ситуацию. Следовательно, чтобы не вводить в заблуждение водителя легкового автомобиля, вы должны включить правый указатель поворота только после въезда на перекресток",
                imageUrl = "id7"
            ), // 7
            Question(
                theme = "Общие вопросы",
                question = "Что называется разрешенной максимальной массой транспортного средства?",
                options = listOf("Максимально допустимая для перевозки масса груза, установленная предприятием-изготовителем.", "Масса снаряженного транспортного средства без учета массы водителя, пассажиров и груза, установленная предприятием-изготовителем.", "Масса снаряженного транспортного средства с грузом, водителем и пассажирами, установленная предприятием-изготовителем в качестве максимально допустимой."),
                correctAnswer = "Масса снаряженного транспортного средства с грузом, водителем и пассажирами, установленная предприятием-изготовителем в качестве максимально допустимой.",
                hint = "Разрешенная максимальная масса - масса снаряженного транспортного средства с грузом, водителем и пассажирами, установленная предприятием-изготовителем в качестве максимально допустимой",
            ), // 8
            Question(
                theme = "Запрещающие знаки",
                question = "Вам разрешено продолжить движение:",
                options = listOf("Только прямо.", "Прямо или в обратном направлении.", "Во всех направлениях."),
                correctAnswer = "Прямо или в обратном направлении.",
                hint = "Знак 3.18.2  \"Поворот налево запрещен\" запрещает только поворот налево как таковой. Таким образом, на этом перекрестке вы можете развернуться в разрыве разделительной полосы или продолжить движение в прямом направлении.",
                imageUrl = "id9"
            ), // 9

        )



        withContext(Dispatchers.IO) {
            questionsToAdd.forEach { question ->
                if (!questionDao.isQuestionExists(question.question)) {
                    questionDao.insert(question)
                }
            }
        }
    }

    suspend fun addInitialTicketsIfNotExist(context: Context) {
        val db = getDatabase(context)
        val ticketDao = db.ticketDao()

        val ticketsToAdd = listOf(
            Ticket(questions = listOf(1, 2, 3, 4, 5, 6, 7)),
            Ticket(questions = listOf(8, 9))
        )

        withContext(Dispatchers.IO) {
            ticketsToAdd.forEach { ticket ->
                if (!ticketDao.isTicketExists(ticket.questions)) {
                    ticketDao.insert(ticket)
                }
            }
        }
    }
}