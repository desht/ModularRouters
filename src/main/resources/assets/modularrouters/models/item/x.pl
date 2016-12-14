#! /usr/bin/perl -w
    eval 'exec /usr/bin/perl -S $0 ${1+"$@"}'
        if 0; #$running_under_some_shell

use strict;
use File::Find ();

# Set the variable $File::Find::dont_use_nlink if you're using AFS,
# since AFS cheats.

# for the convenience of &wanted calls, including -eval statements:
use vars qw/*name *dir *prune/;
*name   = *File::Find::name;
*dir    = *File::Find::dir;
*prune  = *File::Find::prune;

sub wanted;



# Traverse desired filesystems
File::Find::find({wanted => \&wanted}, '.');
exit;


sub wanted {
    my ($dev,$ino,$mode,$nlink,$uid,$gid);

    (($dev,$ino,$mode,$nlink,$uid,$gid) = lstat($_)) &&
    -f _
    && process($name);
}

sub process {
	my $f = shift;
	my $f2 = $f;
	$f2 =~ s/([A-Z])/_\l$1/g;
	print "mv $f $f2\n" unless $f eq $f2
}

