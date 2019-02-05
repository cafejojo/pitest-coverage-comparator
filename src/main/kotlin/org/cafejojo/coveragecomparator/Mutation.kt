package org.cafejojo.coveragecomparator

data class Mutation(
    val detected: Boolean,
    val status: String,
    val location: Location,
    val mutator: String
) {
    fun killed() = detected && status == "KILLED"
    fun coversSameMutationTypeAndLocationAs(other: Mutation) = location == other.location && mutator == other.mutator
}

data class Location(
    val sourceFile: String,
    val mutatedClass: String,
    val mutatedMethod: String,
    val methodDescription: String,
    val lineNumber: Int,
    val index: Int,
    val block: Int
)
