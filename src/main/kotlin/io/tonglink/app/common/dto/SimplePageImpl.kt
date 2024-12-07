package io.tonglink.app.common.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.function.Function

@JsonIgnoreProperties(ignoreUnknown = true)
class SimplePageImpl<T>(
    @JsonProperty("content") content: List<T>,
    @JsonProperty("number") number: Int,
    @JsonProperty("size") size: Int,
    @JsonProperty("totalElements") totalElements: Long
) : Page<T> {

    private val delegate: Page<T> = PageImpl(content, PageRequest.of(number, size), totalElements)

    @JsonProperty
    override fun getTotalPages(): Int = delegate.totalPages

    @JsonProperty
    override fun getTotalElements(): Long = delegate.totalElements

    @JsonProperty("number")
    override fun getNumber(): Int = delegate.number

    @JsonProperty
    override fun getSize(): Int = delegate.size

    @JsonProperty
    override fun getNumberOfElements(): Int = delegate.numberOfElements

    @JsonProperty
    override fun getContent(): List<T> = delegate.content

    @JsonProperty
    override fun hasContent(): Boolean = delegate.hasContent()

    @JsonIgnore
    override fun getSort(): Sort = delegate.sort

    @JsonProperty
    override fun isFirst(): Boolean = delegate.isFirst

    @JsonProperty
    override fun isLast(): Boolean = delegate.isLast

    @JsonIgnore
    override fun hasNext(): Boolean = delegate.hasNext()

    @JsonIgnore
    override fun hasPrevious(): Boolean = delegate.hasPrevious()

    @JsonIgnore
    override fun nextPageable(): Pageable = delegate.nextPageable()

    @JsonIgnore
    override fun previousPageable(): Pageable = delegate.previousPageable()

    override fun <U> map(converter: Function<in T, out U>): Page<U> = delegate.map(converter)

    @JsonIgnore
    override fun iterator(): MutableIterator<T> = delegate.iterator()

    @JsonProperty
    override fun getPageable(): Pageable = delegate.pageable
}