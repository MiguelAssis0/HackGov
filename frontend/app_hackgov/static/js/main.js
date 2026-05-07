const swiperEquipe = new Swiper('.equipe-swiper', {
    loop: true,
    grabCursor: true,
    centeredSlides: true,
    slidesPerView: 3,        // fixo em 3, sempre

    effect: 'coverflow',
    coverflowEffect: {
        rotate: -40,
        stretch: 0,
        depth: 120,
        modifier: 1,
        slideShadows: false,
    },

    pagination: {
        el: '.swiper-pagination',
        clickable: true,
    },

    navigation: {
        nextEl: '.swiper-button-next',
        prevEl: '.swiper-button-prev',
    },

    breakpoints: {
        0: {
            slidesPerView: 1,
        },
        768: {
            slidesPerView: 3,
        },
    }
});