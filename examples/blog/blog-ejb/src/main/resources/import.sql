insert into blog (name, description, locale, timezone, password) values ('JBoss Seam Blog', 'An example of a RESTful Seam application', 'en_AU', 'PST', 'tokyo')
insert into hitcount (blog_name, pageviews) values ('JBoss Seam Blog', 1000)
insert into blogentry (blog_name, id, title, excerpt, body, date) values ('JBoss Seam Blog', 'seamtext', 'Introducing Seam Text', null, 'Seam Text is a human-friendly language for formatting text in blogs, forums and wikis. It''s easy to enter text in *emphasis*, |monospace|, ~deleted~ or ^superscript^; you can easily enter links, lists, quotes and code blocks.', '2007-01-18 14:00:00')
insert into blogentry (blog_name, id, title, excerpt, body, date) values ('JBoss Seam Blog', 'book', 'Seam book excerpt on InfoQ', null, 'Michael and Thomas are writing [a book=>http://safari.oreilly.com/013241273X] about Seam for [O''Reilly=>http://www.oreillynet.com/], which is apparently the hottest selling item on Rough Cuts. InfoQ are running an [excerpt=>http://www.infoq.com/articles/jboss-seam] from the first chapter. Meanwhile, Norman and I have been tearing our way through JIRA issues, and so 1.1.1 will be ready to go when I get back from vacation :-)', '2006-12-19 19:00:00')
insert into blogentry (blog_name, id, title, excerpt, body, date) values ('JBoss Seam Blog', 'roadmap11', 'Seam Roadmap', 'The [1.1 release=>http://blog.hibernate.org/cgi-bin/blosxom.cgi/2006/12/13#seam11-goes-ga] involved a lot of hard work in the guts of Seam, refactoring and fixing bugs, and working with \"tricky\" code that handles concerns like clustering and concurrency. Sure, there are many new features, but a lot of the real work is less user-visible. Now that this hard stuff is out of the way, it lets us add some interesting new stuff pretty quickly. Here''s a quick roadmap for the next few months:', 'The [1.1 release=>http://blog.hibernate.org/cgi-bin/blosxom.cgi/2006/12/13#seam11-goes-ga] involved a lot of hard work in the guts of Seam, refactoring and fixing bugs, and working with \"tricky\" code that handles concerns like clustering and concurrency. Sure, there are many new features, but a lot of the real work is less user-visible. Now that this hard stuff is out of the way, it lets us add some interesting new stuff pretty quickly. Here''s a quick roadmap for the next few months.', '2006-12-14 15:29:00')