-- ============================================================
-- Eventra — V2__seed_reference_data.sql
-- Date de referință: vendor_categories, event_types, counties
-- Versiune: 1.0 | Data: Mai 2026
-- ============================================================

-- ── VENDOR CATEGORIES ───────────────────────────────────────
INSERT INTO vendor_categories (code, name, description, sort_order) VALUES
    ('LOC',   'Locații & Spații',        'Restaurante, saloane, vile, grădini, hoteluri',         1),
    ('FOTO',  'Foto & Video',            'Fotografi, videografi, drone, photo booth',              2),
    ('MUZ',   'Muzică & Entertainment', 'Formații live, DJ, animatori, prezentatori, artiști',    3),
    ('DEC',   'Decor & Flori',           'Floriști, decoratori, lumânări, baloane',                4),
    ('FOOD',  'Catering & Food',         'Catering, cofetărie, candy bar, baruri tematice',        5),
    ('TRANS', 'Transport',               'Mașini de ceremonie, microbuze, limuzine',               6),
    ('INV',   'Invitații & Print',       'Design invitații, mărturii, papetărie de nuntă',         7),
    ('PLAN',  'Planning & Coordonare',   'Wedding planners, event coordinatori, hostess',          8),
    ('BEAUTY','Beauty & Fashion',        'Coafură, make-up, rochii, costume',                      9),
    ('TECH',  'Tehnologie Events',       'Sonorizare, lumini, LED, live streaming',                10);

-- ── EVENT TYPES ─────────────────────────────────────────────
INSERT INTO event_types (code, name, icon, sort_order) VALUES
    -- Evenimente private principale
    ('NUNTA',          'Nuntă',                    'rings',          1),
    ('CUNUNIE_CIVILA', 'Cununie Civilă',            'building',       2),
    ('CUNUNIE_REL',    'Cununie Religioasă',        'church',         3),
    ('BOTEZ',          'Botez',                     'baby',           4),
    ('ANIVERSARE',     'Aniversare',                'cake',           5),
    ('ONOMASTICA',     'Onomastică',                'gift',           6),
    ('PETRECERE',      'Petrecere Privată',         'party-popper',   7),
    ('LOGODNA',        'Logodnă',                   'heart',          8),
    ('BABY_SHOWER',    'Baby Shower',               'baby',           9),
    ('MAJORAT',        'Petrecere Majorat',         'cake',           10),
    -- Evenimente corporate
    ('TEAM_BUILDING',  'Team Building',             'users',          11),
    ('CORPORATE',      'Petrecere Corporativă',     'building-2',     12),
    ('CONFERINTA',     'Conferință / Seminar',      'presentation',   13),
    ('LANSARE',        'Lansare de Produs',         'rocket',         14),
    ('GALA',          'Gală / Dinner Formal',       'award',          15),
    -- Altele
    ('ALTELE',         'Alt tip de eveniment',      'calendar',       16);

-- ── COUNTIES (Județele României) ────────────────────────────
INSERT INTO counties (code, name) VALUES
    ('AB', 'Alba'),
    ('AR', 'Arad'),
    ('AG', 'Argeș'),
    ('BC', 'Bacău'),
    ('BH', 'Bihor'),
    ('BN', 'Bistrița-Năsăud'),
    ('BT', 'Botoșani'),
    ('BV', 'Brașov'),
    ('BR', 'Brăila'),
    ('B',  'București'),
    ('BZ', 'Buzău'),
    ('CS', 'Caraș-Severin'),
    ('CL', 'Călărași'),
    ('CJ', 'Cluj'),
    ('CT', 'Constanța'),
    ('CV', 'Covasna'),
    ('DB', 'Dâmbovița'),
    ('DJ', 'Dolj'),
    ('GL', 'Galați'),
    ('GR', 'Giurgiu'),
    ('GJ', 'Gorj'),
    ('HR', 'Harghita'),
    ('HD', 'Hunedoara'),
    ('IL', 'Ialomița'),
    ('IS', 'Iași'),
    ('IF', 'Ilfov'),
    ('MM', 'Maramureș'),
    ('MH', 'Mehedinți'),
    ('MS', 'Mureș'),
    ('NT', 'Neamț'),
    ('OT', 'Olt'),
    ('PH', 'Prahova'),
    ('SM', 'Satu Mare'),
    ('SJ', 'Sălaj'),
    ('SB', 'Sibiu'),
    ('SV', 'Suceava'),
    ('TR', 'Teleorman'),
    ('TM', 'Timiș'),
    ('TL', 'Tulcea'),
    ('VS', 'Vaslui'),
    ('VL', 'Vâlcea'),
    ('VN', 'Vrancea');

-- ── CHECKLIST TEMPLATES — NUNTĂ ─────────────────────────────
-- Template checklist pre-populat pentru tipul NUNTA
-- days_before = cu câte zile înainte de eveniment trebuie completat

INSERT INTO checklist_templates (event_type_id, title, days_before, sort_order)
SELECT et.id, item.title, item.days_before, item.sort_order
FROM event_types et
CROSS JOIN (VALUES
    -- Cu 12+ luni înainte
    ('Stabilește data nunții',                          365, 1),
    ('Stabilește bugetul total',                        365, 2),
    ('Alege tipul nunții (civil, religios, destinație)',365, 3),
    ('Fă lista preliminară de invitați',                365, 4),
    -- Cu 10-12 luni înainte
    ('Vizitează și rezervă locația',                    300, 5),
    ('Angajează fotograful',                            300, 6),
    ('Angajează videograful',                           300, 7),
    ('Angajează formația / DJ-ul',                      300, 8),
    -- Cu 8-10 luni înainte
    ('Alege și comandă rochia de mireasă',              240, 9),
    ('Angajează wedding planner-ul (opțional)',         240, 10),
    ('Stabilește tema și paleta de culori',             240, 11),
    -- Cu 6-8 luni înainte
    ('Angajează decoratorul / floristul',               180, 12),
    ('Rezervă servicii catering / meniu',               180, 13),
    ('Alege și comandă invitațiile',                    180, 14),
    ('Planifică luna de miere',                         180, 15),
    -- Cu 4-6 luni înainte
    ('Trimite invitațiile',                             120, 16),
    ('Angajează make-up artist și coafor',              120, 17),
    ('Comandă tortul de nuntă',                         120, 18),
    ('Rezervă mașinile de ceremonie',                   120, 19),
    -- Cu 2-4 luni înainte
    ('Confirmă disponibilitatea tuturor furnizorilor',  60, 20),
    ('Finalizează lista de invitați și RSVP-urile',     60, 21),
    ('Planifică seating-ul la mese',                    60, 22),
    ('Comandă verighetele',                             60, 23),
    ('Stabilește meniul definitiv cu locația',          60, 24),
    -- Cu 1-2 luni înainte
    ('Probă finală rochie mireasă',                     30, 25),
    ('Confirmă programul cu preotul / ofițerul stării civile', 30, 26),
    ('Pregătește plicurile / mărturiile',               30, 27),
    ('Confirmă numărul final de invitați la locație',   21, 28),
    -- Cu 1-2 săptămâni înainte
    ('Pregătește timeline-ul zilei nunții',             14, 29),
    ('Trimite timeline-ul tuturor furnizorilor',        14, 30),
    ('Confirmă detaliile cu toți furnizorii',            7, 31),
    ('Pregătește plicul cu plăți pentru furnizori',      3, 32),
    -- Ziua nunții
    ('Verifică că toți furnizorii au sosit',             0, 33)
) AS item(title, days_before, sort_order)
WHERE et.code = 'NUNTA';

-- Template checklist pentru BOTEZ
INSERT INTO checklist_templates (event_type_id, title, days_before, sort_order)
SELECT et.id, item.title, item.days_before, item.sort_order
FROM event_types et
CROSS JOIN (VALUES
    ('Stabilește data botezului',                       90, 1),
    ('Alege nașii',                                     90, 2),
    ('Rezervă locația',                                 60, 3),
    ('Angajează fotograful',                            60, 4),
    ('Stabilește meniul cu locația',                    45, 5),
    ('Trimite invitațiile',                             30, 6),
    ('Comandă tortul',                                  21, 7),
    ('Pregătește darurile pentru nași',                 14, 8),
    ('Confirmă numărul de invitați la locație',         7,  9),
    ('Confirmă toți furnizorii',                        3,  10)
) AS item(title, days_before, sort_order)
WHERE et.code = 'BOTEZ';

-- Template checklist pentru TEAM_BUILDING
INSERT INTO checklist_templates (event_type_id, title, days_before, sort_order)
SELECT et.id, item.title, item.days_before, item.sort_order
FROM event_types et
CROSS JOIN (VALUES
    ('Stabilește obiectivele evenimentului',            60, 1),
    ('Aprobă bugetul intern',                           60, 2),
    ('Alege locația și activitățile',                   45, 3),
    ('Trimite invitațiile / calendar intern',           30, 4),
    ('Confirmă participanții',                          14, 5),
    ('Pregătește materialele și agenda',                7,  6),
    ('Confirmă transportul (dacă e cazul)',             7,  7),
    ('Confirmă locația și cateringul',                  3,  8)
) AS item(title, days_before, sort_order)
WHERE et.code = 'TEAM_BUILDING';
