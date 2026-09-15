package com.medconnect.interfaces.rest;

import java.util.List;

public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PageResponse() {}

    public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <T> PageResponse<T> of(List<T> items, int page, int size) {
        int paginaSegura = Math.max(page, 0);
        int tamanioSeguro = Math.max(size, 1);
        int desde = Math.min(paginaSegura * tamanioSeguro, items.size());
        int hasta = Math.min(desde + tamanioSeguro, items.size());
        int totalPages = (int) Math.ceil((double) items.size() / tamanioSeguro);
        return new PageResponse<>(items.subList(desde, hasta), paginaSegura, tamanioSeguro, items.size(), totalPages);
    }

    // LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "paginacion
    // falsa" -- of() de arriba asume que "items" es la tabla ENTERA y hace
    // el subList en memoria. Este factory es para el caso real: "content" ya
    // viene paginado desde una consulta SQL con LIMIT/OFFSET (Pageable de
    // Spring Data), y "totalElements" viene de una consulta COUNT aparte --
    // solo arma el sobre de respuesta, sin volver a recortar nada.
    public static <T> PageResponse<T> ofPagina(List<T> content, int page, int size, long totalElements) {
        int paginaSegura = Math.max(page, 0);
        int tamanioSeguro = Math.max(size, 1);
        int totalPages = (int) Math.ceil((double) totalElements / tamanioSeguro);
        return new PageResponse<>(content, paginaSegura, tamanioSeguro, totalElements, totalPages);
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
