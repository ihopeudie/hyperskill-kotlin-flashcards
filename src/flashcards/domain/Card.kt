package flashcards.domain

data class Card(val term: String, val definition: String, var errors: Int = 0) {

    override fun toString(): String {
        return "$term:::$definition:::$errors"
    }
}