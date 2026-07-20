/**
 * Placardo — клиентская логика (jQuery).
 * Всё, что работает без перезагрузки страницы, живёт здесь и ходит в REST API /api/v1.
 */
$(function () {

    /* ---------- CSRF для всех ajax-запросов ----------
       Spring Security требует токен на POST/DELETE/PATCH.
       Thymeleaf кладёт его в <meta>, мы добавляем в заголовок каждого запроса. */
    var csrfToken = $('meta[name="_csrf"]').attr('content');
    var csrfHeader = $('meta[name="_csrf_header"]').attr('content');
    if (csrfToken && csrfHeader) {
        $.ajaxSetup({
            beforeSend: function (xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        });
    }

    /* ---------- Избранное: ♥ без перезагрузки ---------- */
    $(document).on('click', '.js-fav', function () {
        var $btn = $(this);
        var adId = $btn.data('ad-id');
        var isFav = $btn.text().indexOf('♥') === 0;
        $.ajax({
            url: '/api/v1/ads/' + adId + '/favorite',
            method: isFav ? 'DELETE' : 'POST'
        }).done(function (resp) {
            $btn.text(resp.favorite ? '♥ В избранном' : '♡ В избранное');
        }).fail(function (xhr) {
            if (xhr.status === 401 || xhr.status === 403) {
                window.location.href = '/login';
            }
        });
    });

    /* ---------- Комментарии: отправка ajax-ом ---------- */
    $('#comment-form').on('submit', function (e) {
        e.preventDefault();
        var adId = $(this).data('ad-id');
        var body = $('#comment-body').val().trim();
        if (!body) return;
        $.ajax({
            url: '/api/v1/ads/' + adId + '/comments',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ body: body })
        }).done(function (c) {
            var initial = c.authorName ? c.authorName.charAt(0) : '?';
            var $item = $(
                '<div class="comment">' +
                '  <div class="avatar avatar-sm"></div>' +
                '  <div class="comment-body">' +
                '    <p class="comment-head"><span class="c-name"></span>' +
                '       <span class="comment-date">только что</span></p>' +
                '    <p class="c-body"></p>' +
                '  </div>' +
                '</div>');
            $item.find('.avatar').text(initial);
            $item.find('.c-name').text(c.authorName);
            $item.find('.c-body').text(c.body);       // .text() экранирует HTML — защита от XSS
            $('#comment-list').append($item);
            $('#comment-body').val('');
            var $count = $('#comment-count');
            $count.text(parseInt($count.text(), 10) + 1);
        }).fail(function () {
            alert('Не удалось отправить комментарий');
        });
    });

    /* ---------- Галерея: клик по миниатюре ---------- */
    $(document).on('click', '.js-thumb', function () {
        $('#gallery-main').attr('src', $(this).attr('src'));
        $('.js-thumb').removeClass('thumb-active');
        $(this).addClass('thumb-active');
    });

    /* ---------- «Показать ещё»: подгрузка каталога через API ---------- */
    $('#load-more').on('click', function () {
        var $btn = $(this);
        var $grid = $('#ad-grid');
        var nextPage = parseInt($grid.data('page'), 10) + 1;
        $btn.prop('disabled', true).text('Загружаем…');
        $.get('/api/v1/ads', {
            q: $grid.data('q') || '',
            category: $grid.data('category') || '',
            page: nextPage,
            size: 12
        }).done(function (page) {
            page.content.forEach(function (ad) {
                $grid.append(renderCard(ad));
            });
            $grid.data('page', nextPage);
            if (page.last) {
                $btn.parent().remove();
            } else {
                $btn.prop('disabled', false).text('Показать ещё');
            }
        }).fail(function () {
            $btn.prop('disabled', false).text('Показать ещё');
        });
    });

    function renderCard(ad) {
        var price = ad.price != null
            ? Number(ad.price).toLocaleString('ru-RU') + ' ₽'
            : 'Даром';
        var date = ad.createdAt ? ad.createdAt.substring(0, 10).split('-').reverse().join('.') : '';
        var $card = $(
            '<div class="card"><a class="card-link">' +
            '  <div class="card-photo"></div>' +
            '  <div class="card-body">' +
            '    <p class="card-price"></p><p class="card-title"></p><p class="card-meta"></p>' +
            '  </div>' +
            '</a></div>');
        $card.find('.card-link').attr('href', '/ads/' + ad.id);
        if (ad.coverImage) {
            $card.find('.card-photo').append($('<img loading="lazy" alt="">').attr('src', ad.coverImage));
        } else {
            $card.find('.card-photo').append('<div class="card-photo-empty">📷</div>');
        }
        $card.find('.card-price').text(price);
        $card.find('.card-title').text(ad.title);
        $card.find('.card-meta').text((ad.city || 'Онлайн') + ' · ' + date);
        return $card;
    }

    /* ---------- Модерация (админка) ---------- */
    $(document).on('click', '.js-moderate', function () {
        var $btn = $(this);
        var adId = $btn.data('ad-id');
        $.ajax({
            url: '/api/v1/admin/ads/' + adId + '/status',
            method: 'PATCH',
            contentType: 'application/json',
            data: JSON.stringify({ status: $btn.data('status') })
        }).done(function () {
            $('[data-row-ad="' + adId + '"]').fadeOut(200, function () { $(this).remove(); });
        }).fail(function () {
            alert('Не удалось изменить статус');
        });
    });

    /* ---------- Бан пользователей (админка) ---------- */
    $(document).on('click', '.js-ban', function () {
        var $btn = $(this);
        $.ajax({
            url: '/api/v1/admin/users/' + $btn.data('user-id') + '/ban',
            method: 'PATCH'
        }).done(function (resp) {
            $btn.text(resp.banned ? 'Разбанить' : 'Забанить');
            var $badge = $btn.closest('tr').find('.badge');
            $badge.toggleClass('badge-rejected', resp.banned).toggleClass('badge-active', !resp.banned)
                  .text(resp.banned ? 'Забанен' : 'Активен');
        }).fail(function () {
            alert('Не удалось изменить статус пользователя');
        });
    });

    /* ---------- Удаление фото на форме редактирования ---------- */
    $(document).on('click', '.js-delete-image', function () {
        var $wrap = $(this).closest('.thumb-wrap');
        $.ajax({
            url: '/api/v1/images/' + $(this).data('image-id'),
            method: 'DELETE'
        }).done(function () {
            $wrap.fadeOut(200, function () { $(this).remove(); });
        }).fail(function () {
            alert('Не удалось удалить фото');
        });
    });
});
