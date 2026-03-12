package repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Currency;
import models.Exchange;

public class ExchangeCrud implements CrudRepository<Exchange> {

    @Override
    public Exchange findById(Long id) {
        String sqlCommand = """
                            SELECT * FROM public.Exchange
                            WHERE id = ?
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, id);
            statement.executeQuery();

            var vResultSet = statement.getResultSet();
            if (vResultSet.next()) {
                return new Exchange(
                        vResultSet.getLong("id"),
                        vResultSet.getLong("baseCurrencyId"),
                        vResultSet.getLong("targetCurrencyId"),
                        vResultSet.getDouble("rate"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Exchange> findAll() {
        List<Exchange> entrieslist = new ArrayList<>();
        String sqlCommand = """
                            SELECT * FROM public.Exchange
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.executeQuery();

            var vResultSet = statement.getResultSet();
            while (vResultSet.next()) {
                Exchange entry = new Exchange(
                        vResultSet.getLong("id"),
                        vResultSet.getLong("baseCurrencyId"),
                        vResultSet.getLong("targetCurrencyId"),
                        vResultSet.getDouble("rate"));
                entrieslist.add(entry);
            }

            return entrieslist;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Exchange entity) {
        String sqlCommand = """
                            INSERT INTO public.Exchange (baseCurrencyId, targetCurrencyId, rate)
                            VALUES (?, ?, ?)
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, entity.getBaseCurrencyId());
            statement.setLong(2, entity.getTargetCurrencyId());
            statement.setDouble(3, entity.getRate());
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Exchange entity) {
        String sqlCommand = """
                            UPDATE public.Exchange
                            SET baseCurrencyId = ?, targetCurrencyId = ?, rate = ?
                            WHERE id = ?
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, entity.getBaseCurrencyId());
            statement.setLong(2, entity.getTargetCurrencyId());
            statement.setDouble(3, entity.getRate());
            statement.setLong(4, entity.getId());
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Long id) {
        String sqlCommand = """
                            DELETE FROM public.Exchange
                            WHERE id = ?
                            """;
        try (Connection connection = Utils.ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(sqlCommand)) {

            statement.setLong(1, id);
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
