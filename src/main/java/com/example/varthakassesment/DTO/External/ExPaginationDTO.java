package com.example.varthakassesment.DTO.External;

public class ExPaginationDTO {


        private int page; //Specific page
        private int limit; //number of user
        private int total;
        private int totalPages;
        private boolean hasNextPage;
        private boolean hasPrevPage;

        public ExPaginationDTO() {
        }

        public ExPaginationDTO(int totalPages, int page, int limit, int total, boolean hasNextPage, boolean hasPrevPage) {
                this.totalPages = totalPages;
                this.page = page;
                this.limit = limit;
                this.total = total;
                this.hasNextPage = hasNextPage;
                this.hasPrevPage = hasPrevPage;
        }

        public int getPage() {
                return page;
        }

        public void setPage(int page) {
                this.page = page;
        }

        public int getLimit() {
                return limit;
        }

        public void setLimit(int limit) {
                this.limit = limit;
        }

        public int getTotal() {
                return total;
        }

        public void setTotal(int total) {
                this.total = total;
        }

        public int getTotalPages() {
                return totalPages;
        }

        public void setTotalPages(int totalPages) {
                this.totalPages = totalPages;
        }

        public boolean isHasNextPage() {
                return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
                this.hasNextPage = hasNextPage;
        }

        public boolean isHasPrevPage() {
                return hasPrevPage;
        }

        public void setHasPrevPage(boolean hasPrevPage) {
                this.hasPrevPage = hasPrevPage;
        }
}
